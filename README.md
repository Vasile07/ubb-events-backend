# UBB Events Backend

This repository contains the Java 21 Spring Boot backend for the UBB Events system.

## Structure

- `Model`: domain entities and request/response DTOs.
- `Common`: shared configuration, security placeholders, and utilities.
- `Persistence`: repository and database access.
- `Business`: business service interfaces and implementations.
- `RestServices`: Spring Boot application and REST controllers.

The Gradle subprojects use the dependency graph approved for the backend skeleton. Build and test with the Gradle wrapper:

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

The health endpoint is available at `GET /api/health`. OpenAPI JSON is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

Runtime Supabase JDBC configuration is supplied through `SUPABASE_JDBC_URL`, `SUPABASE_DB_USERNAME`, and `SUPABASE_DB_PASSWORD`. Tests use an H2 in-memory database and do not require an external database.

For local development, copy `.env.example` to `.env` in this backend repository root, beside `settings.gradle`, and replace the placeholder values with your Supabase JDBC values. Spring Boot imports this file when it exists, and `bootRun` uses the backend root as its working directory:

```powershell
Copy-Item .env.example .env
.\gradlew.bat bootRun
```

Keep `.env` local. It is ignored by Git and must never contain values committed to the repository.

## Container deployment

The Docker image packages the Java 21 Spring Boot application. It builds the executable `RestServices` module with the Gradle wrapper and runs the resulting JAR on a Java 21 runtime image. The application binds to `0.0.0.0` and uses the `PORT` environment variable (default `10000`).

The only database for this deployment is the existing Supabase PostgreSQL database. `compose.yaml` starts the backend only; it does not create a database or load seed data.

### Run locally with Docker Compose

1. Copy `.env.example` to `.env` and replace its placeholder values with your Supabase JDBC URL, database username/password, and a long random JWT secret. Keep `.env` local.
2. Build and start the backend:

   ```powershell
   docker compose up --build -d
   ```

3. Check the API health endpoint:

   ```powershell
   Invoke-RestMethod http://localhost:10000/api/health
   ```

   The response should contain `status: UP`. Stop the container with `docker compose down`.

### Deploy to Render

1. In Render, create **New > Web Service** and connect the backend repository.
2. Select the branch to deploy and choose the Docker runtime with `./Dockerfile` as the Dockerfile path.
3. Add these environment variables in the Render service dashboard. Use the values for your Supabase project and generate a strong, private JWT secret:
   - `SUPABASE_JDBC_URL`
   - `SUPABASE_DB_USERNAME`
   - `SUPABASE_DB_PASSWORD`
   - `JWT_SECRET`
4. Let Render provide `PORT`; the container listens on that port and binds to `0.0.0.0`.
5. After deployment, verify `https://<your-render-service>.onrender.com/api/health` returns `status: UP`. OpenAPI is available at `/v3/api-docs`.

Do not commit `.env`, database credentials, JWT secrets, or production data. Render configuration belongs in the Render dashboard; `.env.example` contains placeholders only.
