# Message Service

Spring Boot service that stores and delivers chat messages (MongoDB-backed) and integrates with notifications, search, presence, media, and encryption services.

## Prerequisites
- Java 21
- Maven 3.9+
- MongoDB (local) or a connection string (Atlas)
- (Optional) Docker + Docker Compose

## Environment (.env)
Create `message-service/.env` with your MongoDB connection and optional service overrides:

```
# MongoDB (recommended)
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/message_db

# Optional service overrides (defaults match local stack)
ENCRYPTION_SERVICE_BASE_URL=http://localhost:8082/encryption
NOTIFICATION_SERVICE_BASE_URL=http://localhost:8085
PRESENCE_SERVICE_BASE_URL=http://localhost:8081/presence
SEARCH_SERVICE_BASE_URL=http://localhost:8084/search
MEDIA_SERVICE_BASE_URL=http://localhost:8083/media
```

Atlas-style variables are also present in the repo `.env`, but the default `application.yaml` uses `SPRING_DATA_MONGODB_URI`. If you want to use the `MONGO_*` variables, uncomment the `spring.data.mongodb.uri` block in `api/src/main/resources/application.yaml`.

## Run locally (Maven)
From `message-service/`:

```
./load-env.ps1
```

That script builds the project, loads `.env`, and starts the API module. Manual alternative:

```
$env:SPRING_DATA_MONGODB_URI='mongodb://localhost:27017/message_db'
# optional overrides
$env:ENCRYPTION_SERVICE_BASE_URL='http://localhost:8082/encryption'
$env:NOTIFICATION_SERVICE_BASE_URL='http://localhost:8085'
$env:PRESENCE_SERVICE_BASE_URL='http://localhost:8081/presence'
$env:SEARCH_SERVICE_BASE_URL='http://localhost:8084/search'
$env:MEDIA_SERVICE_BASE_URL='http://localhost:8083/media'

mvn -pl api -am spring-boot:run
```

The service starts on `http://localhost:8080`.

## Run with Docker
From `message-service/`:

```
docker network create chat-net
```

(Only needed once; `docker-compose.yml` expects this external network.) Then:

```
docker compose up --build
```

This starts the message service plus a MongoDB container.

## Useful endpoints
- Health check: `http://localhost:8080/actuator/health`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui`
- REST:
  - `GET /message?channelId=...&pageNo=0&pageSize=20`
  - `GET /message/inbox?userId=...`
  - `GET /message/{id}`
  - `POST /message` (JSON)
  - `POST /message` (multipart with `payload` + optional `files`)
  - `PUT /message`
  - `DELETE /message/{id}`
