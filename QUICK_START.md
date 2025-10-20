# Quick Start: Azure CD Pipeline Setup

## 🚀 5-Minute Setup Checklist

### 1️⃣ Create Azure Resources (15-20 min)

**In Azure Portal:**

1. **PostgreSQL Database**
   ```
   Resource Group: thelittlethings-rg
   Server name: thelittlethings-db
   Tier: Flexible Server (Burstable B1ms)
   Admin: dbadmin / [your-password]
   ```

2. **Backend App Service**
   ```
   Name: thelittlethings-backend
   Runtime: Java 17 / Linux
   Tier: Free F1 or Basic B1
   ```
   
   **Add these App Settings:**
   - `SPRING_DATASOURCE_URL` = Your PostgreSQL JDBC URL
   - `SPRING_DATASOURCE_USERNAME` = dbadmin
   - `SPRING_DATASOURCE_PASSWORD` = [your-password]
   - `SPRING_JPA_HIBERNATE_DDL_AUTO` = update

3. **Frontend Static Web App**
   ```
   Name: thelittlethings-frontend
   Source: GitHub (nas0404/TheLittleThings)
   Branch: main
   App location: /frontend
   Output location: dist
   ```

### 2️⃣ Configure Azure DevOps (10 min)

1. **Create Project** at dev.azure.com
2. **Create Service Connection:**
   - Project Settings → Service connections
   - Azure Resource Manager → Service Principal
   - Name: `AzureServiceConnection`
3. **Connect to GitHub:**
   - Pipelines → Create Pipeline → GitHub
   - Select repository → Existing YAML file
4. **Set Pipeline Variables:**
   - AZURE_STATIC_WEB_APPS_API_TOKEN (secret) = [from Static Web App in Azure Portal]
5. **Create Environment:**
   - Pipelines → Environments → New → Name: `production`

### 3️⃣ Update azure-pipelines.yml

Edit the deployment section with your actual values:

```yaml
# Line ~115: Backend deployment
azureSubscription: 'AzureServiceConnection'
appName: 'thelittlethings-backend'

# Line ~130: Frontend deployment  
azure_static_web_apps_api_token: '$(AZURE_STATIC_WEB_APPS_API_TOKEN)'
```

### 4️⃣ Push and Deploy!

```bash
git add azure-pipelines.yml
git commit -m "Configure Azure CD pipeline"
git push origin main
```

Watch it deploy in Azure DevOps → Pipelines!

---

## 📱 Your Application URLs

After deployment:
- **Frontend**: `https://thelittlethings-frontend.azurestaticapps.net`
- **Backend**: `https://thelittlethings-backend.azurewebsites.net`

---

## 🎯 For Demo

1. Show pipeline running (Azure DevOps)
2. Show Azure resources (Azure Portal)
3. Show live app working
4. Make a code change → push → auto-deploy!

---

## ⚠️ Common Issues

**Backend won't start?**
→ Check App Service → Logs for errors

**Frontend can't reach backend?**
→ Check CORS settings in backend

**Pipeline fails on deploy?**
→ Verify service connection permissions

---

See `AZURE_DEPLOYMENT_GUIDE.md` for detailed instructions!
