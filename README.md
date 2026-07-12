# Ride Booking App Backend

Backend for a ride booking application built with Spring Boot microservices.

## Services

| Service | Port | Description |
| --- | ---: | --- |
| `auth-service` | `8081` | Login, registration, JWT auth |
| `ride-service` | `8082` | Ride booking and ride status management |
| `payment-service` | `8083` | Payment records and payment status |
| `notification-service` | `8084` | Email notifications from ride events |

## Requirements

- Java 17
- MySQL
- Docker Desktop and Docker Compose

## Docker Quick Start

Run the full local stack from the repository root:

```powershell
docker compose up --build
```

This starts MySQL and all four services:

- Auth service on `http://localhost:8081`
- Ride service on `http://localhost:8082`
- Payment service on `http://localhost:8083`
- Notification service on `http://localhost:8084`

To run in the background:

```powershell
docker compose up --build -d
```

## Main Technologies

- Spring Boot
- Spring Security JWT
- MySQL
- AWS SES

## Important API Endpoints

### Auth

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/admin/drivers/create
GET  /api/v1/auth/users/{id}
```

### Rides

```text
POST  /api/v1/rides/estimate
POST  /api/v1/rides
GET   /api/v1/rides/{rideId}
GET   /api/v1/rides/history
GET   /api/v1/rides/available
PATCH /api/v1/rides/{rideId}/assign-driver
PATCH /api/v1/rides/{rideId}/status
```

### Payments

```text
GET   /api/v1/payments/{paymentId}
GET   /api/v1/payments/ride/{rideId}
GET   /api/v1/payments/passenger/{passengerId}
PATCH /api/v1/payments/{paymentId}/status
```

## Environment Variables

Optional variables:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="password"
$env:JWT_SECRET="base64-secret"
```

For notification emails:

```powershell
$env:AWS_ACCESS_KEY_ID="access-key"
$env:AWS_SECRET_ACCESS_KEY="secret-key"
$env:AWS_REGION="us-east-1"
$env:AWS_SES_FROM="verified-email@example.com"
```

## Notes

- Start MySQL before backend services.
- Keep real passwords and API keys in environment variables.
