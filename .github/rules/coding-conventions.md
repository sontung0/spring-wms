# Coding Conventions

- **Must** use Clean Architecture with Spring Boot Modulith — Hexagonal layout per module.
- **Must** use `@NamedInterface` only on application and domain layers that need cross-module access. Infrastructure and presentation are private.
- **Must** implement `BusinessException` for all expected business errors.
- If a module exception needs a custom HTTP status or error code → **must** create `@RestControllerAdvice(assignableTypes = XxxController.class)`.
- **Must** use `@RequiredArgsConstructor` instead of `@Autowired` for constructor injection.
- **Must** use `XxxSpecification` for custom filtering.
- **Must** use `jakarta.validation` annotations on request DTOs.
- **Must** annotate all controllers, DTOs, and endpoints with OpenAPI/Swagger.