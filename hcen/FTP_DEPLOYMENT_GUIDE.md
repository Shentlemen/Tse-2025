# FTP Deployment Setup Guide

This guide walks you through setting up FTP deployment for HCEN to Virtuozzo/Jelastic.

## Prerequisites

- Access to ANTEL mi-nube (Virtuozzo) dashboard
- WildFly environment created in Virtuozzo
- FTP credentials from Virtuozzo

---

## Step 1: Get FTP Credentials from Virtuozzo

### Option A: Via Virtuozzo Dashboard

1. **Login to ANTEL mi-nube**
   - URL: Provided by ANTEL (e.g., `https://app.minube.antel.com.uy`)
   - Use your credentials

2. **Create or select your environment**
   - If new: Click **"New Environment"**
     - Application Server: **WildFly 37.0.0**
     - Java Version: **21**
     - Environment Name: **hcen-prod**
     - Click **Create**
   - If existing: Select your environment from the list

3. **Get FTP credentials**
   - Click on your environment
   - Click **"Settings"** (gear icon) → **"FTP/FTPS"** tab
   - You'll see something like:
     ```
     Host: ftp.app.minube.antel.com.uy
     Port: 21
     User: node123456-7890
     Password: ●●●●●●●● [Show]
     ```
   - Click **"Show"** to reveal password
   - **Copy these values** - you'll need them in Step 2

4. **Find deployment path**
   - Typical WildFly path: `/opt/jboss/wildfly/standalone/deployments/`
   - Or connect via FTP client to verify

### Option B: Request from ANTEL Support

If you don't have dashboard access:

**Email:** soporte@minube.antel.com.uy

**Subject:** FTP Credentials for HCEN Deployment

**Body:**
```
Hi,

I need FTP access to deploy a Jakarta EE application (WAR file) for the HCEN project.

Project: HCEN - Historia Clínica Electrónica Nacional
Repository: gitlab.fing.edu.uy/agustin.silvano/tse-2025
Student: Agustin Silvano (CI: 5.096.964-8)

Please provide:
1. FTP host and port
2. FTP username and password
3. WildFly deployment directory path
4. Environment URL (e.g., https://hcen-prod.minube.antel.com.uy)

Required environment:
- WildFly 37.0.0
- Java 21
- PostgreSQL 15
- MongoDB 7
- Redis 7

Thanks!
```

---

## Step 2: Configure FTP Credentials in GitLab

1. **Go to your GitLab project**
   - URL: `https://gitlab.fing.edu.uy/agustin.silvano/tse-2025`

2. **Navigate to CI/CD Settings**
   - Left sidebar → **Settings** → **CI/CD**
   - Expand section: **Variables**

3. **Add the following variables**
   Click **"Add variable"** for each:

   | Key | Value (example) | Protected | Masked | Expand |
   |-----|----------------|-----------|--------|---------|
   | `VZ_FTP_HOST` | `ftp.app.minube.antel.com.uy` | ☐ No | ☐ No | ☐ No |
   | `VZ_FTP_PORT` | `21` | ☐ No | ☐ No | ☐ No |
   | `VZ_FTP_USER` | `node123456-7890` | ☐ No | ☐ No | ☐ No |
   | `VZ_FTP_PASSWORD` | `your-actual-password` | ☐ No | ☑️ **YES** | ☐ No |
   | `VZ_DEPLOY_PATH` | `/opt/jboss/wildfly/standalone/deployments/` | ☐ No | ☐ No | ☐ No |

   **Important:**
   - ✅ Check **"Masked"** for `VZ_FTP_PASSWORD` only
   - ❌ Don't check "Protected" (unless you only deploy from protected branches)
   - ❌ Don't check "Expand variable reference"

4. **Save variables**
   - Click **"Add variable"** after each entry
   - Verify all 5 variables are listed

---

## Step 3: Switch to FTP Deployment Pipeline

### Option A: Replace current pipeline (recommended)

```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen

# Backup current pipeline
cp .gitlab-ci.yml .gitlab-ci-docker-backup.yml

# Use FTP deployment
cp .gitlab-ci-ftp.yml .gitlab-ci.yml

# Commit and push
git add .gitlab-ci.yml .gitlab-ci-ftp.yml .gitlab-ci-docker-backup.yml FTP_DEPLOYMENT_GUIDE.md
git commit -m "refactor: switch to FTP deployment (no Docker needed)"
git push origin main
```

### Option B: Test FTP first (safer)

```bash
cd C:\Users\agust\fing\tse\tse-2025\hcen

# Add FTP config alongside current one
git add .gitlab-ci-ftp.yml FTP_DEPLOYMENT_GUIDE.md
git commit -m "feat: add FTP deployment option"
git push origin main
```

Then in GitLab UI:
- Go to **CI/CD → Pipelines**
- Click **"Run pipeline"**
- Click the **"test_ftp_connection"** job (manual)
- If it succeeds, switch to `.gitlab-ci-ftp.yml`

---

## Step 4: Test FTP Connection

1. **Trigger the test job**
   - Go to: **CI/CD → Pipelines**
   - Click **"Run pipeline"**
   - Select branch: **main**
   - Click **"Run pipeline"**

2. **Run FTP connection test**
   - Find job: **test_ftp_connection** (it will show as "manual")
   - Click the **▶ play button**
   - Click job name to see logs

3. **Expected output (success):**
   ```
   Testing FTP connection...
   FTP Host: ftp.app.minube.antel.com.uy:21
   FTP User: node123456-7890

   drwxr-xr-x   2 wildfly  wildfly      4096 Oct 29 10:00 .
   drwxr-xr-x   8 wildfly  wildfly      4096 Oct 28 15:30 ..
   -rw-r--r--   1 wildfly  wildfly   8775352 Oct 28 16:45 hcen.war
   -rw-r--r--   1 wildfly  wildfly        14 Oct 28 16:45 hcen.war.deployed

   ✓ FTP connection successful!
   FTP credentials are working correctly!
   ```

4. **If it fails:**
   - Check error message in job logs
   - Verify credentials in GitLab Variables
   - Check firewall/network access to FTP host
   - Contact ANTEL support if needed

---

## Step 5: Deploy Your Application

### Deploy to Staging (Automatic)

If you have a `develop` branch:

```bash
git checkout develop
git merge main
git push origin develop
```

The pipeline will automatically:
1. ✅ Build → Test → Package
2. ✅ Deploy to staging via FTP
3. ✅ WildFly auto-deploys the WAR

### Deploy to Production (Manual Approval)

From `main` branch:

1. **Push your code:**
   ```bash
   git checkout main
   git push origin main
   ```

2. **In GitLab UI:**
   - Go to: **CI/CD → Pipelines**
   - Wait for **build**, **test**, **package** stages to complete
   - See **deploy_production_ftp** job (will show as "manual")
   - Click the **▶ play button** to deploy
   - Confirm deployment

3. **Monitor deployment:**
   - Check job logs for upload progress
   - Go to Virtuozzo dashboard → your environment
   - Check WildFly logs (should show deployment starting)
   - Wait for: `hcen.war.deployed` marker file to appear

4. **Verify deployment:**
   - Access: `https://hcen-prod.minube.antel.com.uy/hcen/`
   - Check health endpoint: `https://hcen-prod.minube.antel.com.uy/hcen/health`
   - Test login functionality

---

## How WildFly Auto-Deployment Works

When you upload a WAR file to WildFly's `deployments/` directory:

1. **WAR uploaded** → `hcen.war` appears in directory
2. **WildFly detects** new file (watches directory)
3. **Deployment starts** → WildFly extracts and deploys
4. **Success marker** → `hcen.war.deployed` file created
5. **Application live** → Accessible at `/hcen/` context path

**Deployment states:**
- `hcen.war` - WAR file uploaded
- `hcen.war.isdeploying` - Deployment in progress
- `hcen.war.deployed` - ✓ Deployment successful
- `hcen.war.failed` - ✗ Deployment failed (check logs)

---

## Troubleshooting

### Problem: "FTP connection failed"

**Check:**
```bash
# Test from your local machine
ftp ftp.app.minube.antel.com.uy
# Enter username and password
# If this fails, credentials are wrong or FTP is blocked
```

**Solution:**
- Verify credentials in GitLab Variables
- Check if FING network blocks outbound FTP (port 21)
- Try FTPS (port 990) if available
- Contact ANTEL support

### Problem: "WAR uploaded but not deploying"

**Check Virtuozzo logs:**
1. Go to Virtuozzo dashboard
2. Select your environment
3. Click **"Logs"** → WildFly logs
4. Look for errors like:
   - `ClassNotFoundException`
   - `Missing dependencies`
   - `Database connection failed`

**Common causes:**
- Database not configured (PostgreSQL, MongoDB, Redis)
- Missing environment variables
- Java version mismatch
- WildFly modules missing

### Problem: "Access denied" or "Permission denied"

**Check FTP path:**
```bash
# In FTP connection test job, verify path
cd /opt/jboss/wildfly/standalone/deployments/
```

**Solution:**
- Verify `VZ_DEPLOY_PATH` variable in GitLab
- Check FTP user has write permissions
- Contact ANTEL to grant permissions

### Problem: "Application returns 404"

**Check deployment:**
1. Verify `hcen.war.deployed` marker exists
2. Check context path matches URL: `/hcen/`
3. Check WildFly welcome page: `https://hcen-prod.minube.antel.com.uy/`

**Solution:**
- Access root URL first to verify WildFly is running
- Check WAR file name matches (must be `hcen.war`)
- Review WildFly deployment logs

---

## Pipeline Stages Overview

```
┌─────────────────────────────────────────┐
│ 1. BUILD                                │
│    - Gradle clean assemble              │
│    - Creates WAR file                   │
│    - Artifact: build/libs/*.war         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│ 2. TEST (optional)                      │
│    - Run unit tests                     │
│    - Generate reports                   │
│    - Can fail without blocking deploy   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│ 3. PACKAGE                              │
│    - Gradle war                         │
│    - Final WAR artifact                 │
│    - Size check                         │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
┌───────▼──────┐   ┌────────▼─────────┐
│ STAGING      │   │ PRODUCTION       │
│ (automatic)  │   │ (manual)         │
│              │   │                  │
│ - FTP upload │   │ - Backup old WAR │
│ - Auto-deploy│   │ - FTP upload     │
│              │   │ - Manual trigger │
└──────────────┘   └──────────────────┘
```

---

## Next Steps

1. ✅ Complete Steps 1-3 above
2. ✅ Test FTP connection (Step 4)
3. ✅ Deploy to staging/production (Step 5)
4. 🔧 Configure databases (PostgreSQL, MongoDB, Redis)
5. 🔧 Set up environment variables in Virtuozzo
6. 🔧 Configure SSL certificates
7. 📊 Set up monitoring and logging

---

## Additional Resources

- **Virtuozzo Documentation:** https://docs.jelastic.com/
- **WildFly Deployment Guide:** https://docs.wildfly.org/37/Admin_Guide.html#Deployment
- **GitLab CI/CD Variables:** https://docs.gitlab.com/ee/ci/variables/
- **ANTEL mi-nube Support:** soporte@minube.antel.com.uy

---

## Support

If you encounter issues:

1. **Check job logs** in GitLab CI/CD → Pipelines
2. **Check WildFly logs** in Virtuozzo dashboard
3. **Ask FING IT support:** soporte@fing.edu.uy
4. **Ask ANTEL support:** soporte@minube.antel.com.uy

---

**Last Updated:** 2025-10-29
**Author:** TSE 2025 Group 9
