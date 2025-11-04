# GitLab CI Docker Build Fix - Solution Summary

## Problem
Docker-in-Docker pipeline failing with certificate errors because FING's shared runner `gitlab-runner01` doesn't have privileged mode enabled.

## Solution Applied
Replaced Docker-in-Docker with **Kaniko**, a tool that builds container images without requiring a Docker daemon or privileged mode.

## Changes Made

### 1. Updated `.gitlab-ci.yml`

**Location**: `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci.yml`

#### Removed:
- Docker-in-Docker service
- Global Docker/TLS variables (`DOCKER_HOST`, `DOCKER_TLS_CERTDIR`, etc.)

#### docker_build job:
```yaml
# Before: docker:24.0.5-cli with docker:24.0.5-dind service
# After:  gcr.io/kaniko-project/executor:v1.23.2-debug

image:
  name: gcr.io/kaniko-project/executor:v1.23.2-debug
  entrypoint: [""]
script:
  - /kaniko/executor
    --context "${CI_PROJECT_DIR}/hcen"
    --dockerfile "${CI_PROJECT_DIR}/hcen/Dockerfile"
    --destination "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
    --destination "${CI_REGISTRY_IMAGE}:${CI_COMMIT_REF_SLUG}"
    --cache=true
    --cache-ttl=168h
```

#### docker_release job:
```yaml
# Before: docker:24.0.5-cli (pull, tag, push)
# After:  gcr.io/go-containerregistry/crane:v0.19.1

image: gcr.io/go-containerregistry/crane:v0.19.1
script:
  - crane copy "$CI_REGISTRY_IMAGE:$IMAGE_TAG" "$CONTAINER_RELEASE_IMAGE"
```

### 2. Created Documentation Files

#### `DOCKER_CI_TROUBLESHOOTING.md`
**Location**: `C:\Users\agust\fing\tse\tse-2025\hcen\DOCKER_CI_TROUBLESHOOTING.md`
- Complete troubleshooting guide
- Kaniko configuration details
- Common issues and solutions
- Performance optimization tips

#### `.gitlab-ci-dind-alternatives.yml`
**Location**: `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci-dind-alternatives.yml`
- Backup solution configurations (Buildah, DinD without TLS, Shell runner)
- Debugging job to check runner capabilities
- Instructions for requesting runner admin changes

## Why This Works

| Requirement | Docker-in-Docker | Kaniko |
|-------------|------------------|--------|
| Privileged mode | ✅ Required | ❌ Not needed |
| Docker daemon | ✅ Required | ❌ Not needed |
| Volume sharing | ✅ Required | ❌ Not needed |
| TLS certificates | ✅ Required | ❌ Not needed |
| Userspace execution | ❌ No | ✅ Yes |
| Works on shared runners | ❌ Maybe | ✅ Yes |

## Next Steps

### 1. Test the Fix
```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen
git add .gitlab-ci.yml DOCKER_CI_TROUBLESHOOTING.md .gitlab-ci-dind-alternatives.yml SOLUTION_SUMMARY.md
git commit -m "fix: replace Docker-in-Docker with Kaniko for CI builds

- Replaced docker:24.0.5-dind with Kaniko for image building
- Replaced Docker with crane for image tagging/copying
- Removed global Docker/TLS variables (no longer needed)
- Added comprehensive troubleshooting documentation
- Enabled layer caching with 7-day TTL

Fixes Docker-in-Docker certificate sharing issue on FING's shared
runner (gitlab-runner01) which doesn't have privileged mode enabled.

Kaniko builds container images in userspace without requiring a
Docker daemon or privileged access."

git push origin main
```

### 2. Monitor Pipeline
- Go to GitLab: **CI/CD → Pipelines**
- Watch the `docker_build` job
- Expected: "Building Docker image with Kaniko..."
- Expected: Success with image pushed to Container Registry

### 3. Verify Image
After pipeline succeeds:
```bash
# Check Container Registry in GitLab UI
# Packages & Registries → Container Registry
# Should see: latest, main, <commit-sha>
```

## If Something Goes Wrong

### Scenario 1: Kaniko authentication fails
**Error**: "UNAUTHORIZED: authentication required"
**Fix**: Check GitLab CI/CD variables are available:
- Settings → CI/CD → Variables
- Verify `CI_REGISTRY`, `CI_REGISTRY_USER`, `CI_REGISTRY_PASSWORD` are set

### Scenario 2: Base image pull fails
**Error**: "failed to get filesystem"
**Fix**: Check if `quay.io` is accessible from FING network. If blocked, update Dockerfile:
```dockerfile
FROM docker.io/wildfly/wildfly:37.0.0.Final-jdk21
```

### Scenario 3: Kaniko doesn't work at all
**Fix**: Use alternative solutions from `.gitlab-ci-dind-alternatives.yml`:
1. Try Buildah (similar to Kaniko)
2. Try DinD without TLS (less secure, but might work)
3. Contact FING IT to enable privileged mode or set up shell runner

## Benefits of This Solution

1. **No Runner Admin Required**: Works immediately on shared runners
2. **More Secure**: Userspace execution, no privileged mode
3. **Faster Builds**: Built-in layer caching (7-day TTL)
4. **Standard Dockerfile**: No changes to your Dockerfile needed
5. **GitLab Native**: Automatic registry authentication via CI variables

## Alternative Solutions (If Needed)

See detailed alternatives in `.gitlab-ci-dind-alternatives.yml`:
1. **Buildah** - Red Hat's daemonless builder
2. **DinD without TLS** - Disables certificate verification (less secure)
3. **Shell Runner** - Requires runner admin to install Docker on host
4. **Request Privileged Mode** - Requires runner admin to modify config

## Support

### If Pipeline Still Fails
1. Check `DOCKER_CI_TROUBLESHOOTING.md` for common issues
2. Run the `debug_runner` job from `.gitlab-ci-dind-alternatives.yml`
3. Contact FING IT: soporte@fing.edu.uy

### Documentation Files
- `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci.yml` - Main pipeline (updated)
- `C:\Users\agust\fing\tse\tse-2025\hcen\DOCKER_CI_TROUBLESHOOTING.md` - Troubleshooting guide
- `C:\Users\agust\fing\tse\tse-2025\hcen\.gitlab-ci-dind-alternatives.yml` - Backup solutions
- `C:\Users\agust\fing\tse\tse-2025\hcen\SOLUTION_SUMMARY.md` - This file

## References
- **Kaniko**: https://github.com/GoogleContainerTools/kaniko
- **Crane**: https://github.com/google/go-containerregistry/tree/main/cmd/crane
- **GitLab Docker Builds**: https://docs.gitlab.com/ee/ci/docker/using_docker_build.html

---

**Status**: ✅ Ready to test
**Date**: 2025-10-29
**TSE 2025 - Group 9**
