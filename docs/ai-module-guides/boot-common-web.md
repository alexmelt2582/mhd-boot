# boot-common-web

## 1. Module Purpose
`boot-common-web` centralizes request-entry behavior for Spring MVC applications. It provides global exception handling, request body repeatability, XSS filtering, CORS configuration, Undertow hardening, and MyBatis-related web exception translation.

## 2. Main Components
- `com.mhd.boot.common.web.config.FilterConfig`
  - Registers `XssFilter` and `RepeatableFilter`
- `com.mhd.boot.common.web.handler.GlobalExceptionHandler`
  - Converts business and system exceptions to standard responses
- `com.mhd.boot.common.mybatis.core.handler.MybatisExceptionHandler`
  - Handles duplicate key and MyBatis system exceptions
- `com.mhd.boot.common.web.config.ResourcesConfig`
  - Adds CORS, interceptors, and exception handlers
- `com.mhd.boot.common.web.config.UndertowConfig`
  - Hardens Undertow by blocking unsafe HTTP methods
- `com.mhd.boot.common.web.filter.RepeatableFilter`
  - Wraps JSON requests so the body can be read more than once
- `com.mhd.boot.common.web.filter.XssFilter`
  - Sanitizes request parameters for XSS protection

## 3. Boundaries and Non-Goals
This module does:
- provide standard request-layer infrastructure
- normalize exception output
- add lightweight request hardening
- support repeatable JSON request bodies for logging or signature checks

This module does not:
- implement domain validation rules
- replace an API gateway
- provide a WAF or deep security scanning
- manage business error code definitions by itself

## 4. Runtime Behavior
1. Request enters servlet filter chain
2. `RepeatableFilter` wraps JSON requests if applicable
3. `XssFilter` sanitizes non-excluded write requests
4. Dispatcher forwards to controller/service
5. Web exception handlers convert failures into uniform responses
6. `ResourcesConfig` adds cross-cutting MVC behavior such as CORS and timing interception

## 5. Validation and Filter Rules
- `RepeatableFilter` only wraps requests whose `Content-Type` starts with `application/json`
- `XssFilter` skips `GET` and `DELETE`
- `XssFilter` also respects configured exclude URLs
- `UndertowConfig` blocks `CONNECT`, `TRACE`, and `TRACK`
- `ResourcesConfig` allows all origins, headers, and methods through a CORS filter

## 6. Exception Model
- `BusinessException` -> returned as the business error code and message
- `DuplicateKeyException` -> translated to a conflict-style response
- `MyBatisSystemException` -> translated to a server-side database response
- other `Exception` -> translated to a generic 500 response

## 7. Usage Guidance
Recommended:
- keep controller methods small and let service layer throw business exceptions
- rely on `GlobalExceptionHandler` instead of scattered `try/catch`
- use `RepeatableFilter` when you need request body replay for logs or signature verification
- enable `XssFilter` only when the request patterns benefit from it

Be careful with:
- large request bodies, because repeatable wrapping buffers the body
- file upload requests, because body wrapping may not be appropriate for every upload path
- too-broad exception mapping, because that can hide actionable root causes

## 8. Minimal Example
```java
@PostMapping("/users")
public BaseResponse<Long> createUser(@RequestBody CreateUserRequest request) {
    if (request == null || request.getUsername() == null) {
        throw new BusinessException(ErrorCodeEnum.VALID_FAILED, "username is required");
    }
    Long id = userService.create(request);
    return BaseResultUtils.successOfData(id);
}
```

## 9. Extended Example
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public BaseResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ":" + err.getDefaultMessage())
            .findFirst()
            .orElse("parameter validation failed");
    return BaseResultUtils.error(ErrorCodeEnum.VALID_FAILED, message);
}
```

## 10. AI Reading Guidance
- Prefer the existing filter registration and exception handlers before adding new request-layer infrastructure.
- If the task mentions request body replay, first inspect `RepeatableFilter` and `RepeatedlyRequestWrapper`.
- If the task mentions XSS, first inspect `XssFilter` and the exclude URL configuration.

## 11. Testing Notes
- There are no module-specific tests in the repository for this module.
- If you change filter ordering or exception mappings, add integration tests around the request entry path.
