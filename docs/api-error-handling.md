# API error handling

This document describes the backend's shared API error contract. Keep the global handler and API documentation aligned with these conventions.

## Response format

All API errors use `com.ubb.events.dto.ErrorResponse`:

```json
{
  "status": 400,
  "message": "Request validation failed",
  "errors": [
    {
      "field": "email",
      "message": "must be a valid email address"
    }
  ]
}
```

- `status` is the HTTP status code as an integer.
- `message` is a concise, client-safe summary.
- `errors` is a list of `ErrorDetail` values (`field`, `message`). It defaults to an empty list. Validation failures include useful field-level details; other failures may include details only when they help the frontend or debugging and do not reveal implementation or sensitive business information.
- Responses must not include stack traces, exception class names, internal exception messages, or other implementation details.

## Exception types and status mapping

Shared exception types live in `Common/src/main/java/com/ubb/events/common/errors` (`com.ubb.events.common.errors`). The response DTOs live in `Model/src/main/java/com/ubb/events/dto` (`com.ubb.events.dto`). `RestServices` owns the global `@RestControllerAdvice` and translates exceptions to HTTP responses.

| Exception or failure | HTTP status | Notes |
|---|---:|---|
| `BusinessRuleException` | 400 Bad Request | The request violates an application rule. |
| `NotFoundException` | 404 Not Found | The requested resource does not exist. |
| `ConflictException` | 409 Conflict | The request conflicts with the current resource state. |
| `ValidationException` or Bean Validation failure | 400 Bad Request | Bean Validation errors should identify the relevant fields safely. |
| `ForbiddenException` | 403 Forbidden | The caller is not allowed to perform the action. |
| Any other unhandled exception | 500 Internal Server Error | Use the generic message `An unexpected error occurred` and an empty `errors` list. Do not add a dedicated catch-all exception class. |

Malformed request bodies map to 400 with the same response shape. Unexpected exceptions may be logged for diagnostics, but their details must not be returned to clients.

## Validation and OpenAPI

- Put Bean Validation annotations on request DTOs and trigger validation at the Spring MVC boundary with `@Valid` or the appropriate validation mechanism.
- Keep application/business rules in services; controllers handle request validation, mapping, and HTTP responses.
- Document error response schemas and relevant status codes in the generated springdoc OpenAPI document.
- Lists are not paginated unless a later task changes this project convention.
