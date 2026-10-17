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
