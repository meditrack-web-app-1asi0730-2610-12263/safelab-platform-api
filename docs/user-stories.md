# SafeLab Backend v1 User Stories

| ID | Title | Description | Acceptance Criteria |
| --- | --- | --- | --- |
| TS01 | Authentication API | As a Developer, I want sign-in and sign-up endpoints so that the frontend can authenticate users. | Given valid credentials, when the frontend sends a sign-in request, then the API returns the authenticated user and token. |
| TS02 | Operational data API | As a Developer, I want REST endpoints for assets, sensors, alerts and incidents so that the frontend can replace JSON Server. | Given a collection endpoint, when the frontend sends a GET request, then the API returns JSON data from MySQL. |
| TS03 | API documentation | As a Developer, I want Swagger documentation so that the team can inspect and test all web service endpoints. | Given the API is running, when the user opens /swagger, then the documentation is displayed. |
