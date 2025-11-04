# GitLab CI Docker Build Troubleshooting Guide

## Problem Summary

The GitLab CI pipeline at FING (`gitlab-runner.fing.edu.uy`) is failing during the Docker build stage with Docker-in-Docker (DinD) certificate errors.

### Error Symptoms
```
Failed to initialize: unable to resolve docker endpoint: open /certs/client/ca.pem: no such file or directory
```

Additional errors:
```
ip: can't find device 'ip_tables'
modprobe: can't change directory to '/lib/modules': No such file or directory
mount: permission denied (are you root?)
Could not mount /sys/kernel/security
```

## Root Cause

The shared GitLab runner `gitlab-runner01` at FING **does not have privileged mode enabled**, which is required for Docker-in-Docker to:
- Mount special filesystems (`/sys/kernel/security`, `/proc`, etc.)
- Share TLS certificate volumes between containers
- Load kernel modules
- Access device nodes

## Solution Applied: Kaniko ✅

**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci.yml`

We replaced Docker-in-Docker with **Kaniko**, a Google-developed tool that builds container images **without requiring a Docker daemon or privileged mode**.

### What Changed

#### Before (Docker-in-Docker)
```yaml
docker_build:
  image: docker:24.0.5-cli
  services:
    - docker:24.0.5-dind  # Requires privileged mode
  variables:
    DOCKER_HOST: tcp://docker:2376
    DOCKER_TLS_CERTDIR: "/certs"
  script:
    - docker build -t $IMAGE ...
    - docker push $IMAGE
```

#### After (Kaniko)
```yaml
docker_build:
  image:
    name: gcr.io/kaniko-project/executor:v1.23.2-debug
    entrypoint: [""]
  script:
    - /kaniko/executor
      --context "${CI_PROJECT_DIR}/hcen"
      --dockerfile "${CI_PROJECT_DIR}/hcen/Dockerfile"
      --destination "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
      --cache=true
```

### Key Benefits
- **No privileged mode required** - works on restricted/shared runners
- **Built-in layer caching** - faster subsequent builds
- **Secure by default** - userspace execution, no daemon
- **GitLab Container Registry authentication** - automatic via CI variables

### Additional Changes

1. **Removed global Docker variables** (no longer needed):
   - `DOCKER_HOST`
   - `DOCKER_TLS_CERTDIR`
   - `DOCKER_TLS_VERIFY`
   - `DOCKER_CERT_PATH`

2. **Updated `docker_release` job** to use `crane` instead of Docker:
   ```yaml
   docker_release:
     image: gcr.io/go-containerregistry/crane:v0.19.1
     script:
       - crane copy "$SOURCE_IMAGE" "$DEST_IMAGE"
   ```
   `crane` is a lightweight tool for copying/tagging images without Docker.

## Testing the Fix

1. **Commit and push** the updated `.gitlab-ci.yml`:
   ```bash
   git add .gitlab-ci.yml
   git commit -m "fix: replace Docker-in-Docker with Kaniko for CI builds"
   git push origin main
   ```

2. **Monitor the pipeline** at your GitLab instance:
   - Go to: **CI/CD → Pipelines**
   - Watch the `docker_build` job logs
   - Expected output: "Building Docker image with Kaniko..."

3. **Verify image push**:
   - Check: **Packages & Registries → Container Registry**
   - You should see tags: `$CI_COMMIT_SHORT_SHA` and branch name (e.g., `main`)

## Alternative Solutions (Backup)

If Kaniko doesn't work for any reason, see backup solutions in:
**File**: `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci-dind-alternatives.yml`

### Alternative 1: Buildah
Red Hat's daemonless container builder, similar to Kaniko.

### Alternative 2: DinD without TLS
**WARNING**: Insecure, disables TLS verification.
```yaml
services:
  - name: docker:24.0.5-dind
    command: ["--tls=false"]
variables:
  DOCKER_HOST: tcp://docker:2375
  DOCKER_TLS_CERTDIR: ""
```

### Alternative 3: Shell Runner with Docker
Requires runner admin to:
1. Install Docker on runner host
2. Add `gitlab-runner` user to `docker` group
3. Register a shell executor runner

Contact FING IT support: **soporte@fing.edu.uy**

### Alternative 4: Request Privileged Mode
Ask runner admin to edit `/etc/gitlab-runner/config.toml`:
```toml
[[runners]]
  [runners.docker]
    privileged = true  # Enable this
    volumes = ["/certs/client", "/cache"]
```

## Kaniko Advanced Configuration

### Enable Debug Logging
```yaml
image:
  name: gcr.io/kaniko-project/executor:v1.23.2-debug
  entrypoint: [""]
script:
  - /kaniko/executor --verbosity=debug ...
```

### Customize Caching
```yaml
script:
  - /kaniko/executor
    --cache=true
    --cache-ttl=168h          # 7 days
    --cache-repo=$CI_REGISTRY_IMAGE/cache
```

### Build Arguments
```yaml
script:
  - /kaniko/executor
    --build-arg JAVA_VERSION=21
    --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
```

### Ignore Paths (reduce context size)
Create `.dockerignore` in `hcen/`:
```
.git
.gradle
build/test-results
build/reports
*.md
```

## Common Kaniko Issues

### Issue: "UNAUTHORIZED: authentication required"
**Cause**: GitLab Container Registry authentication failed
**Fix**: Verify CI variables are set:
```bash
# In GitLab: Settings → CI/CD → Variables
echo $CI_REGISTRY          # Should be: registry.gitlab.com
echo $CI_REGISTRY_USER     # Should be: gitlab-ci-token
echo $CI_REGISTRY_PASSWORD # Should be: [MASKED]
```

### Issue: "failed to get filesystem: error getting filesystem info"
**Cause**: Base image pull failed
**Fix**: Check Dockerfile base image accessibility:
```yaml
# If quay.io is blocked, use Docker Hub mirror
FROM docker.io/wildfly/wildfly:37.0.0.Final-jdk21
```

### Issue: Slow builds (no cache hit)
**Cause**: Cache not persisting between pipeline runs
**Fix**: Enable distributed caching:
```yaml
--cache=true
--cache-repo="${CI_REGISTRY_IMAGE}/cache"
```

### Issue: "insufficient permissions"
**Cause**: Trying to write to read-only filesystem
**Fix**: Kaniko uses `/workspace` as writable directory by default. Ensure your build doesn't write outside it.

## Monitoring and Debugging

### View Kaniko Build Logs
```bash
# In GitLab pipeline job logs, look for:
INFO[0000] Retrieving image manifest quay.io/wildfly/wildfly:37.0.0.Final-jdk21
INFO[0001] Retrieving image quay.io/wildfly/wildfly:37.0.0.Final-jdk21
INFO[0005] Built cross stage deps: map[]
INFO[0005] Retrieving image manifest quay.io/wildfly/wildfly:37.0.0.Final-jdk21
INFO[0005] Executing 0 build triggers
INFO[0005] COPY ${WAR_PATH}/*.war /opt/jboss/wildfly/standalone/deployments/hcen.war
```

### Check Image Integrity
```bash
# After pipeline succeeds, pull and inspect locally:
docker login $CI_REGISTRY -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD
docker pull $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
docker inspect $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
docker history $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA
```

### Verify WAR Deployment
```bash
# Run container and check WildFly deployment:
docker run -it --rm -p 8080:8080 $CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA

# In another terminal:
curl http://localhost:8080/
# Expected: WildFly welcome page or your app's root endpoint
```

## Performance Optimization

### Reduce Build Context Size
Current context includes entire `hcen/` directory. Optimize with `.dockerignore`:
```bash
cd hcen
cat > .dockerignore <<EOF
# Build artifacts (already copied explicitly)
build/test-results/
build/reports/
build/classes/

# Gradle daemon
.gradle/daemon/
.gradle/native/
.gradle/caches/transforms-*/

# Version control
.git/
.gitignore

# Documentation
*.md
docs/

# IDE files
.idea/
*.iml
*.ipr
*.iws
EOF
```

### Multi-stage Build (future improvement)
To reduce final image size, use multi-stage Dockerfile:
```dockerfile
# Build stage
FROM eclipse-temurin:24-jdk AS builder
WORKDIR /build
COPY . .
RUN ./gradlew war --no-daemon

# Runtime stage
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21
COPY --from=builder /build/build/libs/*.war /opt/jboss/wildfly/standalone/deployments/hcen.war
```

**Current approach**: Build WAR in CI job, copy artifact to Dockerfile
**Multi-stage approach**: Build WAR inside Docker build

## Security Considerations

### Kaniko Security Model
- Runs in **userspace** (no privileged mode)
- Follows **least privilege principle**
- **No Docker daemon** = reduced attack surface
- Uses **unprivileged user** by default

### Container Registry Security
- Uses GitLab's **ephemeral CI tokens** (`gitlab-ci-token`)
- Tokens expire after job completion
- Credentials never stored in logs (masked by GitLab)

### Image Scanning (future enhancement)
Add vulnerability scanning to pipeline:
```yaml
container_scanning:
  stage: test
  image: aquasec/trivy:latest
  script:
    - trivy image --exit-code 1 --severity HIGH,CRITICAL $CI_REGISTRY_IMAGE:$IMAGE_TAG
  needs: ["docker_build"]
```

## References

- **Kaniko Documentation**: https://github.com/GoogleContainerTools/kaniko
- **GitLab Docker Build Best Practices**: https://docs.gitlab.com/ee/ci/docker/using_docker_build.html
- **Crane (image copy tool)**: https://github.com/google/go-containerregistry/tree/main/cmd/crane
- **WildFly Container Images**: https://quay.io/repository/wildfly/wildfly

## Support Contacts

### FING GitLab Support
- **Email**: soporte@fing.edu.uy
- **GitLab Instance**: gitlab-runner.fing.edu.uy
- **Runner Name**: gitlab-runner01

### TSE 2025 Group 9
- German Rodao (4.796.608-7)
- Agustin Silvano (5.096.964-8)
- Piero Santos (6.614.312-9)

## Changelog

### 2025-10-29
- **Fixed**: Replaced Docker-in-Docker with Kaniko for `docker_build` job
- **Fixed**: Replaced Docker with crane for `docker_release` job
- **Removed**: Global Docker/TLS variables (no longer needed)
- **Added**: Kaniko layer caching with 7-day TTL
- **Added**: This troubleshooting guide

---

**Last Updated**: 2025-10-29
**Status**: ✅ Ready for testing
