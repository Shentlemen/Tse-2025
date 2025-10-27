# GitLab Container Registry Setup Guide

## Overview
This guide explains how to properly configure and use GitLab's Container Registry with your CI/CD pipeline for the HCEN project.

---

## What Was Wrong

### 1. Empty `CI_REGISTRY_IMAGE` Variable
**Problem**: The pipeline output showed `Building image :97529ad9` instead of a proper registry URL.

**Root Cause**:
- GitLab's built-in registry variables (`CI_REGISTRY`, `CI_REGISTRY_IMAGE`, etc.) are only populated when the Container Registry feature is **enabled** for your project
- These variables were empty because the registry wasn't activated

### 2. Docker Daemon Connection Error
**Problem**: `error during connect: Post "http://docker:2375/v1.24/auth": dial tcp: lookup docker`

**Root Causes**:
- The separate `push_image` job tried to push an image that didn't exist in its Docker daemon
- Docker images built in one CI job don't persist to the next job (each job runs in a fresh container)
- The Docker service name resolution wasn't working properly

### 3. Incorrect Pipeline Architecture
**Problem**: Splitting `docker_build` and `push_image` into separate stages

**Why This Failed**:
- Each GitLab CI job runs in an isolated environment
- Docker images exist only in the daemon's memory/cache within that job
- When `push_image` started, it had a fresh Docker daemon with no images
- There's no built-in way to transfer Docker images between jobs without a registry

---

## How GitLab Container Registry Works

### Built-in Registry Variables
GitLab automatically provides these predefined variables **when the Container Registry is enabled**:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `CI_REGISTRY` | Registry URL | `registry.gitlab.com` |
| `CI_REGISTRY_IMAGE` | Full image path with registry | `registry.gitlab.com/username/project/image` |
| `CI_REGISTRY_USER` | Registry username (for login) | `gitlab-ci-token` |
| `CI_REGISTRY_PASSWORD` | Registry password (for login) | Auto-generated token |

**Important**: These are automatically populated. You don't need to configure them manually in CI/CD settings.

### Registry Path Structure
Your images will be stored at:
```
registry.gitlab.com/<namespace>/<project-name>/<image-name>:<tag>
```

For your project, this would look like:
```
registry.gitlab.com/agust/tse-2025/hcen:97529ad9
registry.gitlab.com/agust/tse-2025/hcen:latest
```

---

## How to Enable GitLab Container Registry

### Step 1: Enable Container Registry for Your Project

1. Go to your GitLab project: `https://gitlab.com/<your-username>/tse-2025`
2. Navigate to **Settings** → **General**
3. Expand **Visibility, project features, permissions**
4. Find **Container Registry** and toggle it **ON**
5. Click **Save changes**

![GitLab Registry Enable](https://docs.gitlab.com/ee/user/packages/container_registry/img/container_registry_enable_v15_0.png)

### Step 2: Verify Registry is Active

After enabling, you should see a new menu item:
- Go to **Deploy** → **Container Registry** in your project sidebar
- You should see "There are no container images stored for this project"

### Step 3: Run Your Pipeline

Once enabled, the next pipeline run will automatically have access to:
- `CI_REGISTRY` = `registry.gitlab.com`
- `CI_REGISTRY_IMAGE` = `registry.gitlab.com/<namespace>/tse-2025/hcen`
- `CI_REGISTRY_USER` = `gitlab-ci-token`
- `CI_REGISTRY_PASSWORD` = (auto-generated)

---

## What Was Fixed in the Pipeline

### Changes Made to `.gitlab-ci.yml`

#### 1. Consolidated Build and Push into Single Job
**Before** (WRONG):
```yaml
stages:
  - docker_build
  - push

docker_build:
  stage: docker_build
  script:
    - docker build -t "$CI_REGISTRY_IMAGE:$IMAGE_TAG" hcen/

push_image:
  stage: push
  script:
    - docker push "$CI_REGISTRY_IMAGE:$IMAGE_TAG"  # IMAGE DOESN'T EXIST!
```

**After** (CORRECT):
```yaml
stages:
  - docker

docker_build_and_push:
  stage: docker
  script:
    - docker build -t "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}" hcen/
    - docker push "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"  # Push immediately
```

#### 2. Added Docker Daemon Connection Configuration
```yaml
variables:
  DOCKER_HOST: tcp://docker:2375  # Explicitly set daemon host
```

#### 3. Added Docker Daemon Readiness Check
```yaml
before_script:
  - until docker info; do sleep 1; done  # Wait for Docker daemon
```

#### 4. Added Proper Variable Quoting
```yaml
# Use ${VAR} syntax with quotes for reliability
- docker build -t "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}" hcen/
```

#### 5. Added Debug Output
```yaml
- echo "Registry: $CI_REGISTRY"
- echo "Registry Image: $CI_REGISTRY_IMAGE"
- echo "Building image ${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
```

---

## Best Practices for GitLab Container Registry

### 1. Build and Push in Same Job
Always build and push in the same job to avoid image persistence issues:
```yaml
docker_job:
  script:
    - docker build -t "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}" .
    - docker push "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
```

### 2. Use Multiple Tags
Tag your images with both commit SHA and `latest`:
```yaml
- docker build -t "${CI_REGISTRY_IMAGE}:${CI_COMMIT_SHORT_SHA}"
               -t "${CI_REGISTRY_IMAGE}:latest" .
- docker push "${CI_REGISTRY_IMAGE}:${CI_COMMIT_SHORT_SHA}"
- docker push "${CI_REGISTRY_IMAGE}:latest"
```

### 3. Login Before Building (Recommended)
Login before building to enable layer caching from registry:
```yaml
- docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
- docker build --cache-from "${CI_REGISTRY_IMAGE}:latest" -t "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}" .
```

### 4. Use Docker-in-Docker (DinD) Properly
```yaml
services:
  - name: docker:24.0.7-dind
    command: ["--tls=false"]  # Disable TLS for simplicity
variables:
  DOCKER_TLS_CERTDIR: ""  # Must be empty when TLS disabled
  DOCKER_HOST: tcp://docker:2375
```

### 5. Clean Up Old Images
Set retention policies in GitLab:
- Go to **Settings** → **Packages & Registries** → **Container Registry**
- Configure cleanup rules (e.g., keep last 10 tags, delete older than 30 days)

---

## Docker-in-Docker (DinD) Configuration

### Standard DinD Setup
```yaml
image: docker:24.0.7

services:
  - name: docker:24.0.7-dind
    command: ["--tls=false"]

variables:
  DOCKER_TLS_CERTDIR: ""
  DOCKER_HOST: tcp://docker:2375
  DOCKER_DRIVER: overlay2

before_script:
  - until docker info; do sleep 1; done  # Wait for daemon
```

### Why This Works
1. **Service Name**: The `docker:dind` service is accessible via hostname `docker`
2. **Port 2375**: Docker daemon listens on this port (unencrypted)
3. **DOCKER_HOST**: Tells Docker CLI where to connect
4. **TLS Disabled**: Simplifies setup (use TLS in production if needed)
5. **Readiness Check**: `docker info` waits until daemon is ready

---

## Authentication and Security

### How Authentication Works
```yaml
- docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
```

- `CI_REGISTRY_USER`: Always `gitlab-ci-token` (special user)
- `CI_REGISTRY_PASSWORD`: Job-specific token (auto-generated per pipeline)
- These credentials are **read-only** for pulling, **write** for pushing to project registry

### Security Best Practices
1. **Never hardcode credentials** - Use GitLab's built-in variables
2. **Use HTTPS** - GitLab registry always uses HTTPS (registry.gitlab.com)
3. **Limit registry visibility** - Configure in Settings → Container Registry
4. **Enable deployment tokens** - For production deployments (read-only)

---

## Pulling Images from GitLab Registry

### In CI/CD Pipeline
```yaml
deploy:
  image: docker:24.0.7
  script:
    - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
    - docker pull "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
    - docker run "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
```

### From Local Machine
```bash
# Login to GitLab registry
docker login registry.gitlab.com

# Pull image
docker pull registry.gitlab.com/<namespace>/tse-2025/hcen:latest

# Run container
docker run -p 8080:8080 registry.gitlab.com/<namespace>/tse-2025/hcen:latest
```

### From Virtuozzo/Production
Create a **Deploy Token**:
1. Go to **Settings** → **Repository** → **Deploy tokens**
2. Name: `virtuozzo-deploy`
3. Scopes: `read_registry`
4. Create token
5. Use in Virtuozzo:
```bash
docker login registry.gitlab.com -u <deploy-token-name> -p <deploy-token>
docker pull registry.gitlab.com/<namespace>/tse-2025/hcen:97529ad9
```

---

## Testing Your Registry Configuration

### Manual Pipeline Test
1. Commit and push your updated `.gitlab-ci.yml`
2. Go to **CI/CD** → **Pipelines**
3. Watch the `docker_build_and_push` job
4. Check the logs for:
   ```
   Registry: registry.gitlab.com
   Registry Image: registry.gitlab.com/<namespace>/tse-2025/hcen
   Building image registry.gitlab.com/<namespace>/tse-2025/hcen:abc1234
   ```
5. After success, go to **Deploy** → **Container Registry**
6. You should see your image with tags

### Local Test (with GitLab Personal Access Token)
```bash
# Create personal access token with 'read_registry' and 'write_registry' scopes
# https://gitlab.com/-/profile/personal_access_tokens

# Login
docker login registry.gitlab.com -u <your-username> -p <personal-access-token>

# Pull image
docker pull registry.gitlab.com/<namespace>/tse-2025/hcen:latest

# Run locally
docker run -p 8080:8080 registry.gitlab.com/<namespace>/tse-2025/hcen:latest
```

---

## Troubleshooting

### Problem: CI_REGISTRY_IMAGE is still empty

**Solution**:
1. Verify Container Registry is enabled (Settings → General → Container Registry)
2. Check project visibility (registry might be disabled for private projects on free tier)
3. Run pipeline again after enabling registry

### Problem: "unauthorized: authentication required"

**Solution**:
```yaml
# Make sure login happens BEFORE build/push
- docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
- docker push "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
```

### Problem: "manifest unknown" when pulling

**Solution**:
- The image/tag doesn't exist yet
- Check **Deploy** → **Container Registry** to see available tags
- Make sure push was successful in previous job

### Problem: Docker daemon not ready

**Solution**:
```yaml
before_script:
  - until docker info; do sleep 1; done
```

### Problem: Image builds but push fails with "denied"

**Solution**:
- Check if you have permissions (Maintainer or Owner role required)
- Verify registry is enabled for your project
- Check if registry storage quota is exceeded

---

## Complete Working Example

Here's the complete working `docker_build_and_push` job:

```yaml
docker_build_and_push:
  stage: docker
  image: docker:24.0.7
  services:
    - name: docker:24.0.7-dind
      command: ["--tls=false"]
  needs: ["package"]
  dependencies:
    - package
  variables:
    DOCKER_TLS_CERTDIR: ""
    DOCKER_HOST: tcp://docker:2375
  before_script:
    - cd "$CI_PROJECT_DIR"
    - until docker info; do sleep 1; done
  script:
    # Debug output
    - echo "Registry: $CI_REGISTRY"
    - echo "Registry Image: $CI_REGISTRY_IMAGE"
    - echo "Building image ${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"

    # Login to GitLab Container Registry
    - docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"

    # Build with multiple tags
    - docker build
        -t "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
        -t "${CI_REGISTRY_IMAGE}:latest"
        hcen/

    # Verify images
    - docker images

    # Push to registry
    - docker push "${CI_REGISTRY_IMAGE}:${IMAGE_TAG}"
    - docker push "${CI_REGISTRY_IMAGE}:latest"

    - echo "Successfully pushed ${CI_REGISTRY_IMAGE}:${IMAGE_TAG} and ${CI_REGISTRY_IMAGE}:latest"
  only:
    - main
    - develop
    - tags
```

---

## Summary

### What You Need to Do

1. **Enable Container Registry** in GitLab project settings
2. **Commit the updated `.gitlab-ci.yml`** (already done in this fix)
3. **Push to GitLab** and watch the pipeline run
4. **Verify images** appear in Deploy → Container Registry

### What GitLab Provides Automatically

- `CI_REGISTRY` - Registry hostname
- `CI_REGISTRY_IMAGE` - Full image path
- `CI_REGISTRY_USER` - Authentication user
- `CI_REGISTRY_PASSWORD` - Authentication token

### Key Takeaways

- Always **build and push in the same job**
- Use **Docker-in-Docker (DinD)** service for building images in CI
- **Enable Container Registry** in project settings first
- Use **built-in variables** - don't configure registry URLs manually
- **Login before pushing** to authenticate
- **Quote variables** properly: `"${VAR}"`

---

## Additional Resources

- [GitLab Container Registry Docs](https://docs.gitlab.com/ee/user/packages/container_registry/)
- [GitLab CI Docker Build Guide](https://docs.gitlab.com/ee/ci/docker/using_docker_build.html)
- [Docker-in-Docker Service](https://docs.gitlab.com/ee/ci/docker/using_docker_build.html#use-docker-in-docker)
- [GitLab Predefined Variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html)

---

**Author**: TSE 2025 Group 9
**Last Updated**: 2025-10-26
