# GitLab CI/CD Docker Build Configuration Guide

## Summary

This document explains the official GitLab-recommended approach for building Docker images in GitLab CI/CD, specifically for the HCEN Jakarta EE application.

## What Changed

### Previous Configuration Issues

The original configuration had several problems that prevented Docker-in-Docker from starting:

1. **TLS Configuration Mismatch**: Used `DOCKER_TLS_CERTDIR: ""` (TLS disabled) with `DOCKER_HOST: tcp://docker:2375` (non-TLS port), but the DinD service was trying to start with TLS by default
2. **Over-complicated Wait Loop**: 90-second wait loop with manual diagnostics suggested underlying configuration issues
3. **Inconsistent Service Configuration**: Service was configured with `command: ["--storage-driver=overlay2", "--tls=false"]` which conflicts with the default TLS setup

### New Configuration (RECOMMENDED)

The updated `.gitlab-ci.yml` follows GitLab's official best practices:

- **TLS Enabled** (secure by default): `DOCKER_HOST: tcp://docker:2376` with `DOCKER_TLS_CERTDIR: "/certs"`
- **Automatic Certificate Sharing**: Docker automatically generates and shares TLS certificates between the service and job containers
- **Simplified Before Script**: No manual wait loops needed - Docker daemon starts reliably with proper TLS configuration
- **Official Image Versions**: Using `docker:24.0.5-cli` and `docker:24.0.5-dind` as recommended by GitLab
- **Standard Login Method**: `echo "$CI_REGISTRY_PASSWORD" | docker login $CI_REGISTRY -u $CI_REGISTRY_USER --password-stdin`

## Official GitLab Documentation References

This configuration is based on:

1. **Build and push container images to the container registry**
   - URL: https://docs.gitlab.com/user/packages/container_registry/build_and_push_images/
   - Provides the complete multi-stage pipeline example

2. **Use Docker to build Docker images**
   - URL: https://docs.gitlab.com/ci/docker/using_docker_build/
   - Explains Docker-in-Docker configuration, TLS setup, and security considerations

## Key GitLab CI/CD Variables (Automatic)

GitLab provides these variables automatically when the Container Registry is enabled:

- `CI_REGISTRY`: The address of the GitLab Container Registry (e.g., `registry.gitlab.com`)
- `CI_REGISTRY_IMAGE`: The full path to your project's registry (e.g., `registry.gitlab.com/group/project`)
- `CI_REGISTRY_USER`: Username for registry authentication (typically `gitlab-ci-token`)
- `CI_REGISTRY_PASSWORD`: Password/token for registry authentication (automatically generated)
- `CI_COMMIT_SHORT_SHA`: Short commit SHA (first 8 characters, used as image tag)
- `CI_COMMIT_REF_SLUG`: Branch/tag name sanitized for use in URLs and image tags

You do NOT need to manually configure these variables - they are provided by GitLab.

## Configuration Explained

### TLS-Enabled Configuration (Recommended)

```yaml
variables:
  DOCKER_HOST: tcp://docker:2376          # Port 2376 = TLS enabled
  DOCKER_TLS_CERTDIR: "/certs"            # Directory for TLS certificates
  DOCKER_TLS_VERIFY: 1                    # Enable TLS verification
  DOCKER_CERT_PATH: "$DOCKER_TLS_CERTDIR/client"  # Client certificate path

docker_build:
  stage: docker
  image: docker:24.0.5-cli                # Docker CLI image
  services:
    - docker:24.0.5-dind                  # Docker-in-Docker service
  before_script:
    - cd "$CI_PROJECT_DIR"
    # Login to GitLab Container Registry
    - echo "$CI_REGISTRY_PASSWORD" | docker login $CI_REGISTRY -u $CI_REGISTRY_USER --password-stdin
  script:
    # Build with --pull to fetch latest base image
    - docker build --pull -t "$CI_REGISTRY_IMAGE:$IMAGE_TAG" hcen/
    # Push to registry
    - docker push "$CI_REGISTRY_IMAGE:$IMAGE_TAG"
```

### How TLS Works

1. **DinD Service Starts**: The `docker:24.0.5-dind` service container starts with TLS enabled by default
2. **Certificate Generation**: Docker automatically generates TLS certificates in `/certs/server` (service) and `/certs/client` (job)
3. **Certificate Sharing**: GitLab Runner shares the `/certs` directory between service and job containers
4. **Secure Connection**: The job container uses client certificates to securely connect to the Docker daemon on port 2376
5. **No Wait Loops Needed**: The connection is established automatically when Docker is ready

### Benefits of TLS Configuration

- **Security**: Encrypted communication between job and Docker daemon
- **Reliability**: Official GitLab-supported configuration, tested on GitLab.com shared runners
- **Simplicity**: No manual wait loops or diagnostics needed
- **Best Practice**: Follows GitLab's official documentation

## Alternative: Non-TLS Configuration

An alternative configuration without TLS is provided in `.gitlab-ci-no-tls.yml.example`. This may be faster but is less secure.

### When to Use Non-TLS

- Development/testing environments only
- When GitLab Runner is on a trusted, isolated network
- When TLS configuration causes issues with specific runner setups

### Non-TLS Configuration

```yaml
variables:
  DOCKER_HOST: tcp://docker:2375          # Port 2375 = non-TLS
  DOCKER_TLS_CERTDIR: ""                  # Empty = TLS disabled

docker_build:
  services:
    - name: docker:24.0.5-dind
      command: ["--tls=false"]            # Explicitly disable TLS
```

**Note**: GitLab recommends using TLS for production environments.

## Pipeline Architecture

### Stage Flow

```
build → test → package → docker → deploy
```

### Docker Stage Jobs

1. **docker_build** (runs on main, develop, tags):
   - Builds Docker image from Dockerfile
   - Tags with commit SHA and branch/tag slug
   - Pushes both tags to GitLab Container Registry
   - Runs on all protected branches and tags

2. **docker_release** (main branch only):
   - Pulls the commit-specific image
   - Tags as `latest`
   - Pushes `latest` tag to registry
   - Only runs after successful `docker_build`

3. **deploy_virtuozzo** (main branch only, manual):
   - Deploys the built image to Virtuozzo/Jelastic platform
   - Requires manual approval (safety gate for production)
   - Uses custom deployment script

### Image Tagging Strategy

The pipeline creates multiple tags for each build:

- `$CI_REGISTRY_IMAGE:$CI_COMMIT_SHORT_SHA` - Specific commit (e.g., `abc12345`)
- `$CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG` - Branch/tag name (e.g., `main`, `develop`, `v1.0.0`)
- `$CI_REGISTRY_IMAGE:latest` - Latest stable release (main branch only)

This allows you to:
- Deploy specific commits for rollback
- Track which version is running in each environment
- Always pull the latest stable version

## GitLab Runner Requirements

For this configuration to work, the GitLab Runner must be configured with:

### Docker Executor

```toml
[[runners]]
  executor = "docker"
  [runners.docker]
    privileged = true                    # Required for Docker-in-Docker
    volumes = ["/certs/client", "/cache"]  # Share TLS certificates
```

### Kubernetes Executor

```yaml
runners:
  config: |
    [[runners]]
      [runners.kubernetes]
        [[runners.kubernetes.volumes.empty_dir]]
          name = "docker-certs"
          mount_path = "/certs/client"
          medium = "Memory"
```

**Note**: GitLab.com shared runners are already configured correctly for Docker-in-Docker with TLS.

## Troubleshooting

### Issue: "Cannot connect to Docker daemon"

**Symptoms**: Job fails with `Cannot connect to the Docker daemon at tcp://docker:2376. Is the docker daemon running?`

**Solution**: Ensure TLS variables are set correctly:
```yaml
DOCKER_HOST: tcp://docker:2376
DOCKER_TLS_CERTDIR: "/certs"
```

### Issue: "Docker daemon timeout"

**Symptoms**: Job times out waiting for Docker daemon to start

**Possible Causes**:
1. Runner not configured with privileged mode
2. TLS certificate directory not shared
3. Conflicting DOCKER_HOST and DOCKER_TLS_CERTDIR values

**Solution**: Use the TLS-enabled configuration exactly as provided in the official example.

### Issue: "x509: certificate signed by unknown authority"

**Symptoms**: TLS certificate verification fails

**Solution**: Ensure `DOCKER_TLS_CERTDIR` is set to `/certs` (not empty), and `DOCKER_HOST` uses port 2376.

### Issue: Build works but push fails

**Symptoms**: `docker build` succeeds but `docker push` fails with authentication error

**Solution**: Check that Container Registry is enabled in GitLab project settings:
- Go to Settings > General > Visibility, project features, permissions
- Enable "Container Registry"

### Issue: "Error response from daemon: No such image"

**Symptoms**: Dockerfile references base image that cannot be pulled

**Solution**: Verify the base image exists and is accessible:
```dockerfile
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21
```

Check Quay.io or Docker Hub to ensure the image tag is correct.

## Alternatives to Docker-in-Docker

GitLab documentation mentions several alternatives for building container images:

### 1. Kaniko (No Longer Maintained)

**Status**: GitLab previously recommended Kaniko but notes it is no longer actively maintained.

**Advantages**:
- No privileged mode required
- Builds in userspace without Docker daemon
- More secure than Docker-in-Docker

**Configuration Example**:
```yaml
build:
  stage: build
  image:
    name: gcr.io/kaniko-project/executor:latest
    entrypoint: [""]
  script:
    - mkdir -p /kaniko/.docker
    - echo "{\"auths\":{\"$CI_REGISTRY\":{\"auth\":\"$(echo -n $CI_REGISTRY_USER:$CI_REGISTRY_PASSWORD | base64)\"}}}" > /kaniko/.docker/config.json
    - /kaniko/executor --context $CI_PROJECT_DIR/hcen --dockerfile $CI_PROJECT_DIR/hcen/Dockerfile --destination $CI_REGISTRY_IMAGE:$CI_COMMIT_TAG
```

**Note**: Use with caution as the project is no longer maintained.

### 2. Buildah (Rootless Alternative)

**Status**: Actively maintained by Red Hat

**Advantages**:
- Rootless container builds
- No daemon required
- OCI-compliant
- Strong security model

**Configuration Example**:
```yaml
build:
  stage: build
  image: quay.io/buildah/stable:latest
  script:
    - buildah bud -t $CI_REGISTRY_IMAGE:$IMAGE_TAG hcen/
    - buildah push --creds $CI_REGISTRY_USER:$CI_REGISTRY_PASSWORD $CI_REGISTRY_IMAGE:$IMAGE_TAG
```

### 3. Podman

**Status**: Actively maintained by Red Hat

**Advantages**:
- Drop-in replacement for Docker
- Rootless and daemonless
- Compatible with Docker CLI
- Supports Docker Compose (with podman-compose)

**Configuration Example**:
```yaml
build:
  stage: build
  image: quay.io/podman/stable:latest
  script:
    - podman login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - podman build -t $CI_REGISTRY_IMAGE:$IMAGE_TAG hcen/
    - podman push $CI_REGISTRY_IMAGE:$IMAGE_TAG
```

### Recommendation

For this project, **stick with Docker-in-Docker with TLS** (the current configuration) because:

1. **Official Support**: GitLab officially supports and maintains Docker-in-Docker
2. **Shared Runners**: GitLab.com shared runners are optimized for DinD
3. **Simplicity**: Well-documented and widely used
4. **Compatibility**: Works with standard Dockerfiles without modifications
5. **Team Familiarity**: Most developers are familiar with Docker

If security is a major concern and you're running your own GitLab Runners, consider Buildah or Podman as alternatives.

## Best Practices

### 1. Pin Image Versions

Always specify exact image versions instead of using `latest`:

```yaml
image: docker:24.0.5-cli      # Good
image: docker:latest           # Bad - can break unexpectedly
```

### 2. Use `--pull` Flag

Always pull the latest base image to ensure security updates:

```bash
docker build --pull -t $IMAGE_TAG .
```

### 3. Avoid Building to `latest` Tag Directly

Don't build directly to `latest` when multiple concurrent jobs might run. Instead:

1. Build with commit-specific tag
2. Pull that image in a separate job
3. Tag as `latest`
4. Push `latest` tag

This prevents race conditions when multiple builds run simultaneously.

### 4. Cache Base Images

GitLab Runners cache Docker layers automatically, but you can improve build speed by:

- Using multi-stage builds to separate build and runtime dependencies
- Ordering Dockerfile commands from least to most frequently changed

### 5. Secure Registry Credentials

Never hardcode registry credentials. Always use GitLab CI/CD variables:

```yaml
# Good - uses provided variables
echo "$CI_REGISTRY_PASSWORD" | docker login $CI_REGISTRY -u $CI_REGISTRY_USER --password-stdin

# Bad - hardcoded credentials
docker login -u myuser -p mypassword registry.example.com
```

### 6. Monitor Build Times

Track Docker build times and optimize:

- Check Dockerfile layer caching efficiency
- Minimize number of layers
- Combine RUN commands where appropriate
- Use `.dockerignore` to exclude unnecessary files

## Java/Jakarta EE Specific Considerations

### Base Image Selection

For WildFly applications, use official images:

```dockerfile
FROM quay.io/wildfly/wildfly:37.0.0.Final-jdk21
```

**Benefits**:
- Pre-configured WildFly installation
- Regular security updates
- Optimized for production use
- Official support from WildFly team

### WAR File Placement

The Dockerfile copies the WAR file from Gradle build output:

```dockerfile
ARG WAR_PATH=build/libs
COPY ${WAR_PATH}/*.war /opt/jboss/wildfly/standalone/deployments/hcen.war
```

**Important**: The GitLab CI job must run after the `package` stage to ensure the WAR file exists:

```yaml
docker_build:
  needs: ["package"]      # Wait for package job
  dependencies:
    - package             # Download artifacts from package job
```

### JVM Memory Configuration

Configure appropriate heap sizes for containerized deployments:

```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:MaxMetaspaceSize=256m"
```

Adjust based on:
- Container resource limits (ANTEL mi-nube configuration)
- Application memory requirements
- Expected concurrent users

### Health Checks

WildFly provides a health endpoint at `/health`:

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9990/health || exit 1
```

This allows container orchestration platforms (Kubernetes, Docker Swarm, Virtuozzo) to:
- Detect when the application is ready
- Restart unhealthy containers
- Route traffic only to healthy instances

## References

### Official GitLab Documentation

- [Build and push container images to the container registry](https://docs.gitlab.com/user/packages/container_registry/build_and_push_images/)
- [Use Docker to build Docker images](https://docs.gitlab.com/ci/docker/using_docker_build/)
- [Run your CI/CD jobs in Docker containers](https://docs.gitlab.com/ci/docker/using_docker_images/)
- [Build Docker images with BuildKit](https://docs.gitlab.com/ci/docker/using_buildkit/)

### WildFly Documentation

- [WildFly Official Container Images](https://quay.io/repository/wildfly/wildfly)
- [WildFly Docker Documentation](https://github.com/wildfly/wildfly-docker)

### Docker Documentation

- [Docker-in-Docker Best Practices](https://docs.docker.com/engine/security/rootless/)
- [Multi-stage Builds](https://docs.docker.com/develop/develop-images/multistage-build/)

### Alternative Tools

- [Buildah](https://buildah.io/)
- [Podman](https://podman.io/)
- [Kaniko (archived)](https://github.com/GoogleContainerTools/kaniko)

## Getting Help

If you encounter issues with the Docker build pipeline:

1. **Check GitLab CI/CD job logs** for specific error messages
2. **Verify Container Registry is enabled** in project settings
3. **Ensure GitLab Runner has privileged mode** enabled (for self-hosted runners)
4. **Review GitLab documentation** links provided above
5. **Test locally** using the same Docker commands from the CI script
6. **Check runner configuration** if using self-hosted GitLab Runners

## Version History

- **2025-10-29**: Initial version based on GitLab official documentation
  - Implemented TLS-enabled Docker-in-Docker configuration
  - Added multi-tag strategy for image versioning
  - Separated build and release jobs for better control
  - Aligned with GitLab best practices for Jakarta EE applications
