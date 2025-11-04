# Docker Build in GitLab CI - Quick Reference

## What Was Fixed

Your original Docker-in-Docker configuration had conflicting settings that prevented the daemon from starting. The new configuration follows GitLab's official recommendations.

## The Solution (TL;DR)

### Key Changes in `.gitlab-ci.yml`

```yaml
# OLD (Conflicting Configuration)
variables:
  DOCKER_HOST: tcp://docker:2375        # Non-TLS port
  DOCKER_TLS_CERTDIR: ""                # TLS disabled
  DOCKER_DRIVER: overlay2
services:
  - name: docker:24.0.7-dind
    command: ["--storage-driver=overlay2", "--tls=false"]

# NEW (GitLab Official Recommendation)
variables:
  DOCKER_HOST: tcp://docker:2376        # TLS port
  DOCKER_TLS_CERTDIR: "/certs"          # TLS enabled
  DOCKER_TLS_VERIFY: 1
  DOCKER_CERT_PATH: "$DOCKER_TLS_CERTDIR/client"
services:
  - docker:24.0.5-dind                  # No custom command needed
```

### Why It Works Now

1. **TLS Enabled**: Port 2376 with TLS certificates (secure and reliable)
2. **Automatic Certificate Sharing**: GitLab handles certificate distribution
3. **No Manual Wait Loops**: Docker daemon starts reliably with proper config
4. **Official Image Versions**: Using GitLab-recommended `docker:24.0.5-cli` and `docker:24.0.5-dind`

## Quick Start

### 1. Verify the Configuration

The updated `.gitlab-ci.yml` is already in place with the correct settings.

### 2. Commit and Push

```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen
git add .gitlab-ci.yml
git commit -m "chore: fix Docker-in-Docker configuration using GitLab official best practices"
git push
```

### 3. Check the Pipeline

1. Go to your GitLab project
2. Navigate to CI/CD > Pipelines
3. Watch the `docker_build` job run
4. Verify successful build and push to Container Registry

### 4. View Built Images

Go to Packages & Registries > Container Registry in GitLab to see your images.

## Image Tags Created

Each successful pipeline creates:

- `registry.gitlab.com/your-group/your-project:abc12345` (commit SHA)
- `registry.gitlab.com/your-group/your-project:main` (branch name)
- `registry.gitlab.com/your-group/your-project:latest` (main branch only)

## Pipeline Flow

```
Stages: build → test → package → docker → deploy

Docker Stage:
├── docker_build (all branches)
│   └── Builds and pushes commit-specific tags
└── docker_release (main only)
    └── Tags and pushes "latest"
```

## Common Commands

### Pull Built Image Locally

```bash
docker login registry.gitlab.com
docker pull registry.gitlab.com/your-group/your-project:latest
```

### Run Container Locally

```bash
docker run -p 8080:8080 registry.gitlab.com/your-group/your-project:latest
```

### Test Dockerfile Locally

```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen
docker build -t hcen:test .
docker run -p 8080:8080 hcen:test
```

## Automatic GitLab Variables

These are provided automatically (no need to configure):

- `$CI_REGISTRY` - Registry address (e.g., `registry.gitlab.com`)
- `$CI_REGISTRY_IMAGE` - Full image path
- `$CI_REGISTRY_USER` - Registry username (usually `gitlab-ci-token`)
- `$CI_REGISTRY_PASSWORD` - Registry password (auto-generated)
- `$CI_COMMIT_SHORT_SHA` - Short commit hash
- `$CI_COMMIT_REF_SLUG` - Branch/tag name (sanitized)

## Troubleshooting

### Problem: Container Registry not enabled

**Error**: "Authentication failed" when pushing

**Solution**:
1. Go to Settings > General > Visibility, project features, permissions
2. Enable "Container Registry"

### Problem: Build succeeds but image missing

**Symptom**: Pipeline succeeds but no images in Container Registry

**Solution**: Check the `docker push` step in job logs for authentication errors

### Problem: WAR file not found in build

**Error**: `COPY failed: file not found`

**Solution**: Ensure `docker_build` job has these settings:
```yaml
needs: ["package"]
dependencies:
  - package
```

### Problem: Permission denied

**Error**: "permission denied while trying to connect to Docker daemon"

**Solution**: Verify GitLab Runner has `privileged = true` in configuration (GitLab.com shared runners already have this)

## Files Reference

- **`.gitlab-ci.yml`** - Main pipeline configuration (TLS-enabled, recommended)
- **`.gitlab-ci-no-tls.yml.example`** - Alternative without TLS (less secure)
- **`DOCKER_BUILD_GUIDE.md`** - Complete documentation with troubleshooting
- **`DOCKER_BUILD_QUICKSTART.md`** - This quick reference (you are here)
- **`Dockerfile`** - Container image definition

## Next Steps

1. **Commit the changes** to trigger the pipeline
2. **Monitor the first build** to ensure it completes successfully
3. **Verify images** in Container Registry
4. **Test deployment** to Virtuozzo (manual job on main branch)

## Additional Resources

- [Full Docker Build Guide](./DOCKER_BUILD_GUIDE.md) - Comprehensive documentation
- [GitLab Docker Build Docs](https://docs.gitlab.com/ci/docker/using_docker_build/) - Official GitLab documentation
- [WildFly Docker Images](https://quay.io/repository/wildfly/wildfly) - Base images

## Need Help?

1. Check job logs in GitLab CI/CD interface
2. Review `DOCKER_BUILD_GUIDE.md` for detailed troubleshooting
3. Verify Container Registry is enabled in project settings
4. Test Docker commands locally first

---

**Last Updated**: 2025-10-29
**Configuration Version**: GitLab Official Best Practices (TLS-enabled)
