# Azure App Service Configuration - Backend

Copy these environment variables to your Azure App Service configuration:

## Required Database Settings

```
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DB_SERVER.postgres.database.azure.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=dbadmin
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## CORS Configuration

```
CORS_ALLOWED_ORIGINS=https://YOUR_FRONTEND_NAME.azurestaticapps.net,http://localhost:5173
```

Replace:
- `YOUR_DB_SERVER` with your PostgreSQL server name (e.g., `thelittlethings-db`)
- `YOUR_DB_PASSWORD` with your database password
- `YOUR_FRONTEND_NAME` with your Static Web App name (e.g., `thelittlethings-frontend`)

## How to Add in Azure Portal

1. Go to Azure Portal
2. Navigate to your App Service (backend)
3. Click "Configuration" in the left menu
4. Click "New application setting" for each variable above
5. Click "Save" at the top
6. Click "Continue" when prompted to restart

## Optional Settings

```
# Logging
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
LOGGING_LEVEL_COM_PROJECT_THELITTLETHINGS=DEBUG

# Server settings (already configured by Azure)
# SERVER_PORT=8080
```

---

# Azure Static Web Apps Configuration - Frontend

Add this environment variable to your Static Web App:

```
VITE_API_URL=https://YOUR_BACKEND_NAME.azurewebsites.net
```

Replace `YOUR_BACKEND_NAME` with your App Service name (e.g., `thelittlethings-backend`)

## How to Add in Azure Portal

1. Go to Azure Portal
2. Navigate to your Static Web App (frontend)
3. Click "Configuration" in the left menu
4. Click "Add" under Application settings
5. Name: `VITE_API_URL`
6. Value: Your backend URL
7. Click "OK"
8. Click "Save"

---

# For Local Development

## Backend (.env or IDE run configuration)
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=YOUR_LOCAL_PASSWORD
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

## Frontend (frontend/.env)
```
VITE_API_URL=http://localhost:8080
```
