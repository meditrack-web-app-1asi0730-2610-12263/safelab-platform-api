# SafeLab Platform API

Spring Boot backend for the SafeLab Web Application.

This version is prepared for **Render + PostgreSQL** and keeps the current frontend `db.json` structure as persistent JSON collections in a relational table named `stored_documents`. This lets the frontend migrate quickly from local mock data to a real backend while keeping all 12 bounded contexts connected through one API.

## Stack

- Java 21
- Spring Boot 3.4
- Spring Web
- Spring Data JPA
- PostgreSQL for Render
- H2 for local quick testing
- Swagger/OpenAPI
- Dockerfile ready for Render
- Seed data from `src/main/resources/db.seed.json`

## Run locally with H2

```bash
mvn spring-boot:run
```

Swagger:

```txt
http://localhost:8080/swagger-ui.html
```

Health check:

```txt
http://localhost:8080/actuator/health
```

## Run locally with PostgreSQL

```bash
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/safelab_db" \
SPRING_DATASOURCE_USERNAME="safelab_user" \
SPRING_DATASOURCE_PASSWORD="your-password" \
mvn spring-boot:run
```

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/safelab_db"
$env:SPRING_DATASOURCE_USERNAME="safelab_user"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
mvn spring-boot:run
```

## Deploy to Render

Create a **Web Service** from the backend repository and use Docker.

Recommended settings:

```txt
Runtime: Docker
Region: Oregon (same as your database)
Branch: main
Root Directory: ./
Health Check Path: /actuator/health
```

Environment variables in the Render Web Service:

```txt
SPRING_PROFILES_ACTIVE=postgres
DATABASE_URL=<paste Render PostgreSQL Internal Database URL>
CORS_ALLOWED_ORIGINS=https://safelab-frontend.vercel.app,http://localhost:5173
SEED_RESET_ON_START=false
JAVA_OPTS=-XX:MaxRAMPercentage=75.0
```

Use the **Internal Database URL** from your Render PostgreSQL service when the backend is also running on Render. It looks like:

```txt
postgres://safelab_user:PASSWORD@HOST:5432/safelab_db
```

The backend automatically converts Render's `postgres://...` URL into the JDBC URL required by Spring Boot.

Alternative manual variables:

```txt
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/safelab_db?sslmode=require
SPRING_DATASOURCE_USERNAME=safelab_user
SPRING_DATASOURCE_PASSWORD=PASSWORD
CORS_ALLOWED_ORIGINS=https://safelab-frontend.vercel.app,http://localhost:5173
```

## API base URL

Local:

```txt
http://localhost:8080/api/v1
```

Render example:

```txt
https://safelab-platform-api.onrender.com/api/v1
```

Swagger on Render:

```txt
https://safelab-platform-api.onrender.com/swagger-ui.html
```

## Demo reset

Reset all collections from seed data:

```txt
POST /api/v1/demo/reset
```

Use only for demo/testing.

## Main collections

Generic CRUD is available for these collections:

```txt
users
facilities
sensors
assets
complianceRules
alerts
notifications
incidents
actuators
remoteCommands
reports
auditLogs
billing
currentUser
```

Example:

```txt
GET    /api/v1/sensors
POST   /api/v1/sensors
GET    /api/v1/sensors/{id}
PATCH  /api/v1/sensors/{id}
DELETE /api/v1/sensors/{id}
```

## Business endpoints

### IAM

```txt
POST /api/v1/auth/sign-in
POST /api/v1/auth/sign-out
GET  /api/v1/auth/me
```

Demo credential:

```json
{
  "email": "admin@safelab.pe",
  "password": "123456"
}
```

### Dashboard

```txt
GET  /api/v1/dashboard/overview
POST /api/v1/dashboard/refresh
```

### Sensor Monitoring

```txt
GET   /api/v1/sensor-monitoring/sensors
POST  /api/v1/sensor-monitoring/sensors
PATCH /api/v1/sensor-monitoring/sensors/{id}/reading
POST  /api/v1/sensor-monitoring/sensors/{id}/disconnect
```

Out-of-range sensor readings create related alerts, notifications and audit logs.

### Alerts & Notifications

```txt
GET  /api/v1/alerts-notifications/alerts
GET  /api/v1/alerts-notifications/notifications
POST /api/v1/alerts-notifications/alerts/{id}/acknowledge
POST /api/v1/alerts-notifications/alerts/{id}/resolve
POST /api/v1/alerts-notifications/alerts/{id}/escalate
POST /api/v1/alerts-notifications/notifications/{id}/mark-read
```

Escalating an alert creates an incident.

### Remote Control

```txt
GET  /api/v1/remote-control/actuators
GET  /api/v1/remote-control/commands
POST /api/v1/remote-control/actuators/{id}/commands
```

Example command:

```json
{
  "command": "Start",
  "requestedBy": "Dr. Maria Lopez"
}
```

### Incidents

```txt
GET  /api/v1/incident-management/incidents
POST /api/v1/incident-management/incidents/{id}/start-investigation
POST /api/v1/incident-management/incidents/{id}/resolve
POST /api/v1/incident-management/incidents/{id}/close
```

### Audit Trail

```txt
GET /api/v1/audit-traceability/logs
GET /api/v1/audit-traceability/timeline
```

## Frontend connection

After the backend is deployed, set this in Vercel for the frontend:

```txt
VITE_API_BASE_URL=https://YOUR_RENDER_BACKEND_URL/api/v1
```

Then the frontend can stop using only localStorage/demo state and start consuming the deployed API.
