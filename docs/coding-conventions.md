# Backend coding conventions

## Java naming

Use PascalCase for classes, interfaces, records, and enums; camelCase for methods, parameters, and fields; and UPPER_SNAKE_CASE for constants. Package names are lowercase. Name tests after the class under test with the `Test` suffix.

## Layering and dependency direction

Controllers handle HTTP mapping, validation, and response codes. Business services contain application rules. Persistence repositories contain database access. Domain entities and request/response DTOs belong in `Model`. Keep dependencies directed through the approved module graph.

## JPA

Use JPA annotations for persistence entities and keep persistence concerns out of controllers and DTOs. Define relationships and fetch behavior explicitly when entities are introduced. Repository interfaces should focus on data access and should not contain business rules.

## Lombok

Use Lombok for constructor, getter, and setter boilerplate when it improves readability. Prefer constructor injection and immutable fields. Add setters only when the object genuinely needs controlled mutation; avoid generating setters indiscriminately for entities, DTOs, or services.

## SOLID and dependency injection

Apply SOLID principles, especially dependency inversion: higher-level business code depends on abstractions, while implementations are supplied by Spring's dependency injection. Use constructor injection so dependencies are explicit, required dependencies cannot be missing, and classes remain straightforward to test. Spring's inversion of control container owns object creation and wiring.

## Object-oriented design

Keep each class focused on one responsibility, encapsulate state and invariants, and prefer composition over inheritance unless an is-a relationship is clear. Use polymorphism behind stable interfaces when multiple implementations are expected. Keep public APIs small and expose behavior rather than internal state.
