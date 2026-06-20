# SafeLab Platform API

## Project Overview

The `SafeLab Platform API` is a backend application designed to support SafeLab's laboratory monitoring web application. It manages identity access, user profiles, assets, sensors, alerts, remote control actions, reports, incidents, audit traceability and subscription data.

Built with .NET and ASP.NET Core, it follows a Domain-Driven Design (DDD) inspired organization by bounded contexts and exposes RESTful endpoints documented through Swagger / OpenAPI.

## Table of Contents

1. Architecture Overview
2. Domain-Driven Design Concepts
3. Key Features Implemented
4. Bounded Contexts
5. Technologies Used
6. Getting Started
7. MySQL Configuration
8. Project Structure
9. API Documentation
10. Deployment on Render
11. Frontend Integration
12. License

## Architecture Overview

The backend is organized around bounded contexts and layered architecture:

- **Domain Layer**: domain models, aggregate roots and repository contracts.
- **Application Layer**: application services that coordinate use cases.
- **Infrastructure Layer**: Entity Framework Core persistence and database initialization.
- **Interfaces Layer**: REST controllers and request/response resources.

This first version uses a generic JSON document persistence strategy in MySQL. This allows the API to replace the existing JSON Server quickly while preserving the frontend data shape. In the final version, these documents can be progressively replaced by strongly typed aggregates and relational tables for each bounded context.

## Domain-Driven Design Concepts

SafeLab is divided into independent bounded contexts:

- IAM
- User Profiles
- Subscription & Billing
- Dashboard & Overview
- Asset & Inventory Monitoring
- Sensor Monitoring
- Environmental Compliance
- Alerts & Notifications
- Remote Control & Actuation
- Reports & Analytics
- Incident Management
- Audit & Traceability
- Shared

Each context has its own folder and contains Application, Domain, Infrastructure and Interfaces layers.

## Key Features Implemented

- RESTful API with ASP.NET Core.
- Swagger / OpenAPI documentation.
- MySQL persistence with Entity Framework Core.
- Seed data imported from the final SafeLab frontend `db.json`.
- Endpoints compatible with JSON Server routes, such as `/users`, `/assets`, `/sensors`, `/alerts`.
- Versioned API routes, such as `/api/v1/users`, `/api/v1/assets`, `/api/v1/sensors`.
- Basic sign-in and sign-up endpoints.
- CORS enabled for frontend integration.
- Dockerfile and `render.yaml` for Render deployment.

## Technologies Used

- **.NET 10**: Core framework for the application.
- **ASP.NET Core**: RESTful API development.
- **C#**: Server-side programming language.
- **Entity Framework Core**: ORM for persistence.
- **MySQL**: Relational database.
- **Swashbuckle.AspNetCore**: Swagger / OpenAPI documentation.
- **Humanizer**: Utility library for naming and formatting support.
- **Docker**: Containerized deployment.
- **Render**: Cloud deployment platform for the backend.

## Getting Started

### Prerequisites

- .NET 10 SDK
- MySQL Server
- Git
- Rider or Visual Studio

### Setup Instructions

1. Clone the repository:

```bash
git clone https://github.com/your-organization/safelab-platform-api.git
cd safelab-platform-api
```

2. Navigate to the project folder:

```bash
cd SafeLab.Platform
```

3. Restore NuGet packages:

```bash
dotnet restore
```

4. Configure MySQL:

Create a database in MySQL:

```sql
CREATE DATABASE safelab_platform_dev;
```

Update the connection string in `SafeLab.Platform/appsettings.Development.json`:

```json
"ConnectionStrings": {
  "DefaultConnection": "server=localhost;port=3306;user=root;password=password;database=safelab_platform_dev"
}
```

5. Run the application:

```bash
dotnet run --project SafeLab.Platform/SafeLab.Platform.csproj
```

6. Open Swagger:

```text
http://localhost:8080/swagger
```

7. Health check:

```text
http://localhost:8080/health
```

## MySQL Configuration

The API creates the database schema automatically on startup using Entity Framework Core `EnsureCreated`. It also seeds data from:

```text
SafeLab.Platform/Data/seed-data.json
```

For this first backend version, the data is stored in a table named:

```text
stored_documents
```

This table contains the collection name, document id and JSON content for each SafeLab record. This keeps compatibility with the current Vue frontend while preparing the backend for a future fully relational model.

## Project Structure

```text
SafeLab.Platform/
├── Iam/
│   ├── Application/
│   ├── Domain/
│   ├── Infrastructure/
│   └── Interfaces/
├── UserProfiles/
├── SubscriptionBilling/
├── DashboardOverview/
├── AssetInventoryMonitoring/
├── SensorMonitoring/
├── EnvironmentalCompliance/
├── AlertsNotifications/
├── RemoteControlActuation/
├── ReportsAnalytics/
├── IncidentManagement/
├── AuditTraceability/
├── Shared/
│   ├── Domain/
│   ├── Infrastructure/
│   └── Interfaces/
├── Data/
│   └── seed-data.json
├── Program.cs
├── appsettings.json
├── appsettings.Development.json
└── SafeLab.Platform.csproj
```

## API Documentation

Swagger is available at:

```text
http://localhost:8080/swagger
```

Main endpoints:

```text
POST /api/v1/authentication/sign-in
POST /api/v1/authentication/sign-up
GET  /api/v1/users
GET  /api/v1/user-profiles
GET  /api/v1/assets
GET  /api/v1/sensors
GET  /api/v1/alerts
GET  /api/v1/notifications
GET  /api/v1/remote-actuators
GET  /api/v1/reports
GET  /api/v1/historical-data
GET  /api/v1/incidents
GET  /api/v1/audit-traceability
GET  /api/v1/subscriptions
GET  /api/v1/billing-plans
```

Compatibility endpoints for the current frontend:

```text
GET /users
GET /userProfiles
GET /assets
GET /sensors
GET /alerts
GET /incidents
GET /reports
```

## Deployment on Render

This repository includes a `Dockerfile` and `render.yaml`.

On Render, create a new Web Service from this repository and set the following environment variable:

```text
MYSQL_CONNECTION_STRING=server=<host>;port=<port>;user=<user>;password=<password>;database=<database>
```

Render will build the Docker image and expose the API. Swagger will be available at:

```text
https://your-render-service.onrender.com/swagger
```

## Frontend Integration

After deploying the backend, configure the frontend environment variable in Vercel:

```text
VITE_API_BASE_URL=https://your-render-service.onrender.com
```

Then redeploy the frontend.

## Demo Credentials

```text
admin@safelab.com / admin123
coordinator@safelab.com / demo123
pharmacy.coordinator@safelab.com / demo123
lab.tech1@safelab.com / demo123
maintenance@safelab.com / demo123
billing@safelab.com / demo123
```

## Documentation

Additional documentation is located in the `docs/` folder:

- `docs/class-diagram.puml`
- `docs/software-architecture.dsl`
- `docs/user-stories.md`

## License

This project is licensed under the MIT License.
