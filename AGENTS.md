# Backend repository guidance

The workspace-level `AGENTS.md` is authoritative for workflow gates, task traceability, secrets, branching, commits, and pushes. This file adds backend-specific conventions only and must be read together with the workspace instructions.

Before changing backend code, read [`docs/coding-conventions.md`](docs/coding-conventions.md) and follow its Java naming, layering, JPA, Lombok, SOLID, dependency-injection, and object-oriented design guidance. This applies to implementers and any other agents working in this repository.

- Keep controllers in `RestServices`, business interfaces in `com.ubb.events`, implementations in `com.ubb.events.implementation`, persistence access in `Persistence`, and models/DTOs in `Model`.
- Keep controllers free of business rules and direct database access. Put business rules in services and database access in repositories.
- Use the Gradle wrapper for all builds and tests. Never commit credentials, `.env` files, or generated build output.
- Preserve the workspace approval gates. Do not commit or push without the required user approval.

If this file conflicts with the workspace-level instructions, the workspace instructions take precedence.
