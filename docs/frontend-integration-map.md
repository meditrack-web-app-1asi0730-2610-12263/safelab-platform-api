# Frontend Integration Map

Use this API base URL in the frontend once the backend is running:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## JSON Server replacement

| Current JSON Server collection | Backend endpoint |
|---|---|
| `/users` | `/api/v1/users` |
| `/facilities` | `/api/v1/facilities` |
| `/sensors` | `/api/v1/sensors` |
| `/assets` | `/api/v1/assets` |
| `/complianceRules` | `/api/v1/complianceRules` |
| `/alerts` | `/api/v1/alerts` |
| `/notifications` | `/api/v1/notifications` |
| `/incidents` | `/api/v1/incidents` |
| `/actuators` | `/api/v1/actuators` |
| `/remoteCommands` | `/api/v1/remoteCommands` |
| `/reports` | `/api/v1/reports` |
| `/auditLogs` | `/api/v1/auditLogs` |

## Recommended business endpoints

Prefer these when adding real backend behavior:

- Dashboard: `/api/v1/dashboard/overview`
- Sensor reading: `/api/v1/sensor-monitoring/sensors/{id}/reading`
- Alert acknowledge: `/api/v1/alerts-notifications/alerts/{id}/acknowledge`
- Alert resolve: `/api/v1/alerts-notifications/alerts/{id}/resolve`
- Alert escalate to incident: `/api/v1/alerts-notifications/alerts/{id}/escalate`
- Remote command: `/api/v1/remote-control/actuators/{id}/commands`
- Incident status flow: `/api/v1/incident-management/incidents/{id}/start-investigation`, `/mark-resolved`, `/close`
- Reports generation: `/api/v1/reports-analytics/reports/generate`
- Audit timeline: `/api/v1/audit-traceability/timeline`
