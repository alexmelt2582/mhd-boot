# boot-common-doc

## 1. Module Purpose
`boot-common-doc` provides SpringDoc and OpenAPI integration for generated API documentation. It centralizes metadata, scanning scope, and doc endpoint behavior so the service layer can expose consistent machine-readable API docs.

## 2. Main Components
- `com.mhd.boot.common.doc.config.SpringDocConfig`
  - SpringDoc and OpenAPI configuration entry
- `com.mhd.boot.common.doc.config.SpringDocProperties`
  - Properties for title, description, version, group, and scan scope
- `com.mhd.boot.common.doc.handler.OpenApiHandler`
  - Additional OpenAPI processing hook

## 3. Boundaries and Non-Goals
This module does:
- configure OpenAPI documentation consistently
- centralize documentation metadata
- expose doc processing hooks when adjustments are needed

This module does not:
- implement business APIs
- replace controller annotations
- author the written description of each endpoint for you

## 4. Runtime Behavior
1. Application starts and loads SpringDoc configuration
2. Metadata from properties is applied to the OpenAPI model
3. Controllers and DTOs are scanned into the generated document
4. Optional handlers adjust the final OpenAPI representation

## 5. Validation and Metadata Rules
- Title, description, and version should be configured explicitly
- Group names should remain stable across releases
- Scan scope should match the intended API surface
- Custom handlers should preserve the actual controller contract

## 6. Exception Model
- Doc generation failures surface during startup or when the doc endpoint is accessed
- Misconfiguration usually appears as missing groups, empty endpoints, or mismatched schema metadata

## 7. Usage Guidance
Recommended:
- keep metadata in properties rather than hardcoding it in several places
- align controller annotations with the generated API model
- use handler hooks for focused schema tweaks instead of rewriting the entire document

Be careful with:
- stale metadata after refactors
- overly broad scan scope
- custom handler logic that distorts the actual controller behavior

## 8. Minimal Example
```java
@Configuration
public class DocConfig {
}
```

## 9. Extended Example
```java
@Bean
public OpenAPI customOpenAPI(SpringDocProperties properties) {
    return new OpenAPI().info(new Info()
            .title(properties.getTitle())
            .description(properties.getDescription())
            .version(properties.getVersion()));
}
```

## 10. AI Reading Guidance
- Reuse `SpringDocConfig` and `SpringDocProperties` before adding custom documentation wiring.
- If the task only needs endpoint discovery, the default SpringDoc model may already be sufficient.
- When docs look wrong, verify the scan scope before changing controllers.

## 11. Testing Notes
- This module currently has no module-specific tests in the repository.
- If you change grouping or metadata mapping, add tests around the OpenAPI bean configuration.
