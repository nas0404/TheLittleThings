# Azure CI/CD Pipeline - Summary

## ✅ What I've Set Up For You

### 1. Updated Azure Pipeline (`azure-pipelines.yml`)
Your pipeline now has **two stages**:

#### **Stage 1: Build & Test (CI)**
- ✅ Builds frontend (React + Vite)
- ✅ Builds backend (Spring Boot + Maven)
- ✅ Runs all unit tests
- ✅ Publishes test results
- ✅ Creates deployment artifacts (JAR + dist folder)

#### **Stage 2: Deploy (CD)**
- ✅ Deploys backend JAR to Azure App Service
- ✅ Deploys frontend to Azure Static Web Apps
- ✅ Only runs if build & tests succeed
- ✅ Uses "production" environment for approval gates

### 2. Added CORS Configuration (`WebConfig.java`)
- ✅ Global CORS configuration for all controllers
- ✅ Configurable via environment variables
- ✅ Works for both local development and Azure production

### 3. Created Documentation
- ✅ `AZURE_DEPLOYMENT_GUIDE.md` - Complete step-by-step guide
- ✅ `QUICK_START.md` - 5-minute quick reference
- ✅ `AZURE_ENV_VARIABLES.md` - Environment variable templates
- ✅ `PIPELINE_SUMMARY.md` - This file!

---

## 📋 What You Need to Do

### Before You Can Deploy:

1. **Create Azure Resources** (20 minutes)
   - [ ] Azure Database for PostgreSQL
   - [ ] Azure App Service (backend)
   - [ ] Azure Static Web App (frontend)
   
   👉 See `AZURE_DEPLOYMENT_GUIDE.md` Section "Phase 1"

2. **Configure Azure DevOps** (10 minutes)
   - [ ] Create Azure DevOps project
   - [ ] Create service connection to Azure
   - [ ] Configure pipeline variables
   - [ ] Create "production" environment
   
   👉 See `AZURE_DEPLOYMENT_GUIDE.md` Section "Phase 2"

3. **Update Pipeline File** (2 minutes)
   - [ ] Replace `YOUR_SERVICE_CONNECTION_NAME` with `AzureServiceConnection`
   - [ ] Replace `YOUR_BACKEND_APP_SERVICE_NAME` with your actual app name
   - [ ] Add Static Web Apps API token as secret variable
   
   👉 Lines ~115-145 in `azure-pipelines.yml`

4. **Configure Environment Variables** (5 minutes)
   - [ ] Add database connection settings to backend App Service
   - [ ] Add CORS allowed origins to backend App Service
   - [ ] Add API URL to frontend Static Web App
   
   👉 See `AZURE_ENV_VARIABLES.md`

5. **Update Frontend API Configuration** (2 minutes)
   - [ ] Use `import.meta.env.VITE_API_URL` in your API calls
   - [ ] Create `frontend/.env` for local development
   
   👉 See example in `AZURE_DEPLOYMENT_GUIDE.md` Section "Phase 3.1"

6. **Push and Deploy!** (1 minute)
   ```bash
   git add .
   git commit -m "Configure Azure CI/CD pipeline"
   git push origin main
   ```

---

## 🎯 Pipeline Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Push to main                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              STAGE 1: BUILD & TEST (CI)                     │
├─────────────────────────────────────────────────────────────┤
│  1. Setup Node.js 20.x                                      │
│  2. Install frontend dependencies (npm ci)                  │
│  3. Build frontend → dist/ folder                           │
│  4. Publish frontend artifact                               │
│  ─────────────────────────────────────────────              │
│  5. Setup Java 17                                           │
│  6. Run Maven tests                                         │
│  7. Publish test results                                    │
│  8. Build JAR file (mvn package)                            │
│  9. Publish backend artifact                                │
└────────────────────────┬────────────────────────────────────┘
                         │
                  Tests Pass? ──No──> ❌ Pipeline Fails
                         │
                        Yes
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              STAGE 2: DEPLOY (CD)                           │
├─────────────────────────────────────────────────────────────┤
│  Deploy Backend Job:                                        │
│    1. Download backend artifact (JAR)                       │
│    2. Deploy to Azure App Service                           │
│    3. App Service automatically runs the JAR                │
│  ─────────────────────────────────────────────              │
│  Deploy Frontend Job:                                       │
│    1. Download frontend artifact (dist)                     │
│    2. Deploy to Azure Static Web Apps                       │
│    3. Static Web App hosts the files                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
                   ✅ Deployed!
```

---

## 🎓 For Your Assignment Demo

### What to Demonstrate:

1. **Show the Code** (2 minutes)
   - Open `azure-pipelines.yml`
   - Explain the two stages: CI and CD
   - Highlight key tasks (build, test, deploy)

2. **Show Azure DevOps** (3 minutes)
   - Open your pipeline
   - Show a successful run
   - Click into stages to show:
     - Frontend build logs
     - Backend test results
     - Deployment logs

3. **Show Azure Resources** (2 minutes)
   - Azure Portal → Resource Group
   - Show App Service (backend) is running
   - Show Static Web App (frontend) is deployed
   - Show PostgreSQL database

4. **Show Live Application** (2 minutes)
   - Open your frontend URL
   - Demo the app working
   - Show it's connected to Azure database

5. **Demonstrate CI/CD** (3 minutes) ⭐ **Most Important!**
   - Make a visible code change (e.g., change homepage text)
   - Commit and push to GitHub
   - Show Azure DevOps automatically triggering
   - Wait for deployment (or show previous successful run)
   - Refresh your live app to show the change

### Talking Points:

**Continuous Integration (CI):**
> "Every time I push code to the main branch, Azure Pipelines automatically:
> - Builds both frontend and backend
> - Runs all unit tests
> - Only proceeds if tests pass
> This ensures broken code never gets deployed."

**Continuous Deployment (CD):**
> "After successful tests, the pipeline automatically:
> - Deploys the backend JAR to Azure App Service
> - Deploys the frontend to Azure Static Web Apps
> - No manual intervention needed
> This means new features go live within minutes."

**Benefits:**
> - ✅ Faster release cycles
> - ✅ Fewer bugs in production (automated testing)
> - ✅ Consistent deployments (no manual errors)
> - ✅ Easy rollbacks (previous versions available)

---

## 🔧 Troubleshooting Common Issues

### Pipeline fails at "Deploy Backend" step
**Error:** "Service connection not found"
**Fix:** Verify service connection name matches in pipeline and Azure DevOps

### Backend starts but returns 500 errors
**Error:** Database connection failed
**Fix:** Check App Service configuration has correct database URL and credentials

### Frontend can't reach backend API
**Error:** CORS errors in browser console
**Fix:** 
1. Add frontend URL to `CORS_ALLOWED_ORIGINS` in backend App Service settings
2. Verify `VITE_API_URL` points to correct backend URL

### Static Web App deployment fails
**Error:** Invalid API token
**Fix:** Generate new deployment token from Azure Portal → Static Web App → Manage deployment token

---

## 💡 Pro Tips

1. **Use Pipeline Variables for Secrets**
   - Never commit passwords or tokens to Git
   - Store them as secret variables in Azure DevOps

2. **Enable Deployment Approvals**
   - Go to Environments → production → Approvals and checks
   - Add yourself as approver
   - Prevents accidental deployments

3. **Monitor Costs**
   - Check Azure Cost Management regularly
   - Free/Basic tiers should be sufficient for demo
   - Delete resources when done to avoid charges

4. **Keep Logs Accessible**
   - Azure Portal → App Service → Log stream
   - Helps debug deployment issues quickly

5. **Test Locally First**
   - Always test changes locally before pushing
   - Saves pipeline minutes and catches issues early

---

## 📊 Expected Timeline

- **Initial Setup**: 30-40 minutes (one-time)
- **Each Deployment**: 3-5 minutes (automatic)
- **Making Changes**: Instant → automatic deployment

---

## 🎉 Success Checklist

You'll know everything is working when:

- [x] Pipeline shows green checkmark ✅ for both stages
- [x] Azure Portal shows all three resources running
- [x] Frontend URL loads your application
- [x] Backend API responds to requests
- [x] Application connects to Azure database
- [x] Code changes auto-deploy within 5 minutes

---

## 📚 Files Created/Modified

### New Files:
- `backend/src/main/java/com/project/thelittlethings/config/WebConfig.java`
- `AZURE_DEPLOYMENT_GUIDE.md`
- `QUICK_START.md`
- `AZURE_ENV_VARIABLES.md`
- `PIPELINE_SUMMARY.md` (this file)

### Modified Files:
- `azure-pipelines.yml` - Added CD stage
- `backend/src/main/resources/application.properties` - Added CORS config

---

## 🚀 Next Steps

1. Read through `QUICK_START.md` for the condensed version
2. Follow `AZURE_DEPLOYMENT_GUIDE.md` step-by-step
3. Use `AZURE_ENV_VARIABLES.md` as reference when configuring
4. Come back to this file when preparing your demo

**Good luck with your assignment!** 🎓

If you run into issues, check the Troubleshooting section in `AZURE_DEPLOYMENT_GUIDE.md`.
