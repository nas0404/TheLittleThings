# Azure CI/CD Pipeline Setup Guide for TheLittleThings

This guide will help you set up a complete CI/CD pipeline for your application using Azure DevOps and Azure services with your student subscription.

## 📋 Overview

Your application consists of:
- **Frontend**: React + TypeScript + Vite
- **Backend**: Spring Boot 3.3.3 (Java 17)
- **Database**: PostgreSQL

## 🎯 Architecture

```
GitHub (main branch) 
    ↓ (trigger)
Azure Pipelines
    ├─ Stage 1: Build & Test (CI)
    │   ├─ Build Frontend
    │   ├─ Build Backend
    │   └─ Run Tests
    └─ Stage 2: Deploy (CD)
        ├─ Deploy Backend → Azure App Service
        ├─ Deploy Frontend → Azure Static Web Apps
        └─ Database → Azure PostgreSQL
```

## 🚀 Step-by-Step Setup

### **Phase 1: Create Azure Resources**

#### 1.1 Azure Database for PostgreSQL

1. Go to [Azure Portal](https://portal.azure.com)
2. Click **"Create a resource"** → Search for **"Azure Database for PostgreSQL"**
3. Choose **"Flexible server"** (more cost-effective)
4. Configure:
   - **Resource Group**: Create new (e.g., `thelittlethings-rg`)
   - **Server name**: `thelittlethings-db` (must be globally unique)
   - **Region**: Choose closest to you
   - **PostgreSQL version**: 15 or latest
   - **Compute + storage**: 
     - Burstable, B1ms (1-2 vCores) - cheapest option
     - 32 GB storage
   - **Admin username**: `dbadmin` (or your choice)
   - **Password**: Create a strong password and **save it securely**
   - **Networking**: 
     - Allow access from Azure services: **Yes**
     - Add your current IP for testing
5. Click **"Review + create"** → **"Create"**
6. After creation, go to **Connection strings** and copy the JDBC URL

#### 1.2 Azure App Service for Backend

1. In Azure Portal, click **"Create a resource"** → **"Web App"**
2. Configure:
   - **Resource Group**: Use same as above (`thelittlethings-rg`)
   - **Name**: `thelittlethings-backend` (must be globally unique)
   - **Publish**: Code
   - **Runtime stack**: Java 17
   - **Java web server stack**: Java SE (Embedded Web Server)
   - **Operating System**: Linux
   - **Region**: Same as your database
   - **Pricing**: 
     - Free (F1) for demo, or
     - Basic (B1) for better performance
3. Click **"Review + create"** → **"Create"**
4. After creation, go to **Configuration** → **Application settings**
5. Add these environment variables (click **"New application setting"** for each):
   ```
   SPRING_DATASOURCE_URL = jdbc:postgresql://YOUR_DB_SERVER.postgres.database.azure.com:5432/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME = dbadmin
   SPRING_DATASOURCE_PASSWORD = YOUR_DB_PASSWORD
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   ```
6. Click **"Save"**

#### 1.3 Azure Static Web Apps for Frontend

1. In Azure Portal, click **"Create a resource"** → **"Static Web App"**
2. Configure:
   - **Resource Group**: Same (`thelittlethings-rg`)
   - **Name**: `thelittlethings-frontend`
   - **Plan type**: Free
   - **Region**: Choose available region
   - **Deployment source**: GitHub
   - **Sign in** to GitHub and authorize Azure
   - **Organization**: Select your GitHub org
   - **Repository**: `TheLittleThings`
   - **Branch**: `main`
   - **Build presets**: Custom
   - **App location**: `/frontend`
   - **Output location**: `dist`
3. Click **"Review + create"** → **"Create"**
4. After creation, go to **Configuration**
5. Add API URL environment variable:
   - Variable name: `VITE_API_URL`
   - Value: `https://thelittlethings-backend.azurewebsites.net` (your backend URL)

---

### **Phase 2: Configure Azure DevOps**

#### 2.1 Create Azure DevOps Project

1. Go to [Azure DevOps](https://dev.azure.com)
2. Sign in with your Azure student account
3. Click **"New project"**
   - Name: `TheLittleThings`
   - Visibility: Private
   - Click **"Create"**

#### 2.2 Connect to GitHub

1. In your Azure DevOps project, go to **Pipelines**
2. Click **"Create Pipeline"**
3. Select **"GitHub"**
4. Authenticate and authorize Azure Pipelines
5. Select your repository: `nas0404/TheLittleThings`
6. Select **"Existing Azure Pipelines YAML file"**
7. Branch: `main`
8. Path: `/azure-pipelines.yml`
9. Click **"Continue"** (don't run yet)

#### 2.3 Create Service Connection

This allows Azure Pipelines to deploy to your Azure resources.

1. In Azure DevOps, go to **Project Settings** (bottom left)
2. Under **Pipelines**, click **"Service connections"**
3. Click **"New service connection"**
4. Select **"Azure Resource Manager"** → **"Next"**
5. Authentication method: **"Service principal (automatic)"**
6. Scope level: **"Subscription"**
7. Select your **Azure subscription**
8. Resource group: `thelittlethings-rg`
9. Service connection name: `AzureServiceConnection`
10. Check **"Grant access permission to all pipelines"**
11. Click **"Save"**

#### 2.4 Configure Pipeline Variables

1. Go to **Pipelines** → Select your pipeline → **Edit**
2. Click the **three dots** (⋮) → **"Variables"**
3. Add these variables:

**Regular Variables:**
- `backendAppServiceName` = `thelittlethings-backend`
- `frontendAppName` = `thelittlethings-frontend`
- `azureServiceConnection` = `AzureServiceConnection`

**Secret Variables** (click the lock icon 🔒 to make them secret):
- `AZURE_STATIC_WEB_APPS_API_TOKEN`:
  1. Go to Azure Portal → Your Static Web App
  2. Click **"Manage deployment token"**
  3. Copy the token
  4. Paste it as the variable value

4. Click **"Save"**

#### 2.5 Update Pipeline Configuration

You need to replace placeholder values in `azure-pipelines.yml`:

**Update these lines in the pipeline:**

1. In the `DeployBackend` job, update:
   ```yaml
   azureSubscription: 'AzureServiceConnection'  # Use your service connection name
   appName: 'thelittlethings-backend'           # Use your actual backend app name
   ```

2. For Static Web Apps deployment:
   - The token is already configured via pipeline variables
   - Or use the App Service option by uncommenting those lines

#### 2.6 Create Environment

1. In Azure DevOps, go to **Pipelines** → **Environments**
2. Click **"New environment"**
3. Name: `production`
4. Resource: None (leave empty)
5. Click **"Create"**

This allows you to add approvals before deployment (optional but recommended).

---

### **Phase 3: Update Application Configuration**

#### 3.1 Update Frontend API URL

Update your frontend to use environment variables for the API URL.

**File: `frontend/src/api/http.ts`** (or wherever you configure axios/fetch):

```typescript
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

For local development, create `frontend/.env`:
```env
VITE_API_URL=http://localhost:8080
```

For production (configured in Azure Static Web Apps):
```env
VITE_API_URL=https://thelittlethings-backend.azurewebsites.net
```

#### 3.2 Update Backend CORS Configuration

Ensure your backend allows requests from your frontend domain.

**File: `backend/src/main/java/com/project/thelittlethings/config/WebConfig.java`** (create if it doesn't exist):

```java
package com.project.thelittlethings.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173",  // Local development
                    "https://thelittlethings-frontend.azurestaticapps.net"  // Production
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

---

### **Phase 4: Deploy!**

#### 4.1 First Deployment

1. **Commit and push** your updated `azure-pipelines.yml`:
   ```bash
   git add azure-pipelines.yml
   git commit -m "Add CD pipeline configuration"
   git push origin main
   ```

2. This will automatically trigger the pipeline in Azure DevOps

3. Monitor the pipeline:
   - Go to Azure DevOps → **Pipelines**
   - Click on the running pipeline
   - Watch each stage complete:
     - ✅ Build Frontend
     - ✅ Build Backend
     - ✅ Run Tests
     - ✅ Deploy Backend
     - ✅ Deploy Frontend

#### 4.2 Verify Deployment

**Backend:**
- URL: `https://thelittlethings-backend.azurewebsites.net`
- Test endpoint: `https://thelittlethings-backend.azurewebsites.net/actuator/health` (if you have Spring Actuator)

**Frontend:**
- URL: `https://thelittlethings-frontend.azurestaticapps.net`
- Open in browser and test functionality

**Database:**
- Check Azure Portal → PostgreSQL → Monitoring to see connection activity

---

## 🎓 For Your Assignment Demonstration

### What to Show:

1. **GitHub Repository**
   - Show your `azure-pipelines.yml` file
   - Explain the stages: Build → Test → Deploy

2. **Azure DevOps Pipeline**
   - Show successful pipeline run
   - Highlight test results
   - Show deployment logs

3. **Azure Resources**
   - Show App Service (backend) running
   - Show Static Web App (frontend) running
   - Show PostgreSQL database

4. **Live Application**
   - Demo your app running on Azure
   - Show it's connected to the cloud database

5. **CI/CD in Action**
   - Make a small code change
   - Push to GitHub
   - Show automatic pipeline trigger
   - Show automatic deployment

### Key Points to Mention:

✅ **Continuous Integration (CI):**
   - Automatically builds on every push to main
   - Runs automated tests
   - Creates build artifacts

✅ **Continuous Deployment (CD):**
   - Automatically deploys on successful builds
   - Separate staging for frontend/backend
   - Environment management

✅ **Best Practices:**
   - Infrastructure as Code (YAML pipeline)
   - Artifact management
   - Environment variables for configuration
   - Automated testing before deployment

---

## 🐛 Troubleshooting

### Pipeline fails to deploy backend:
- Check service connection has correct permissions
- Verify App Service name is correct
- Check JAR file was built successfully

### Frontend can't connect to backend:
- Verify CORS configuration
- Check `VITE_API_URL` environment variable
- Ensure backend is running (check App Service logs)

### Database connection fails:
- Verify firewall rules allow Azure services
- Check connection string is correct
- Verify credentials

### Get App Service logs:
```bash
az webapp log tail --name thelittlethings-backend --resource-group thelittlethings-rg
```

---

## 💰 Cost Management (Student Subscription)

**Free tier options:**
- Static Web Apps: **Free** tier (100 GB bandwidth/month)
- App Service: **F1 Free** tier (60 minutes/day) or **B1 Basic** (~$13/month)
- PostgreSQL: **Burstable B1ms** (~$12-15/month)

**Total estimated cost:** $0-30/month depending on tiers

**After assignment:**
- Delete resource group to stop all charges:
  ```bash
  az group delete --name thelittlethings-rg --yes
  ```

---

## 📚 Additional Resources

- [Azure Pipelines Documentation](https://docs.microsoft.com/azure/devops/pipelines/)
- [Azure App Service Java Deployment](https://docs.microsoft.com/azure/app-service/quickstart-java)
- [Azure Static Web Apps](https://docs.microsoft.com/azure/static-web-apps/)
- [Azure for Students](https://azure.microsoft.com/free/students/)

---

## ✅ Checklist

Before presenting:

- [ ] Azure resources created (Database, Backend App Service, Frontend Static Web App)
- [ ] Service connection configured in Azure DevOps
- [ ] Pipeline variables set
- [ ] `azure-pipelines.yml` updated with correct values
- [ ] Frontend configured with production API URL
- [ ] Backend CORS allows frontend domain
- [ ] Pipeline runs successfully
- [ ] Application accessible and working on Azure
- [ ] Can demonstrate a code change → auto-deploy flow

Good luck with your assignment! 🚀
