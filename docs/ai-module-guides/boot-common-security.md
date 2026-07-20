# boot-common-security

## 1. Module Purpose
`boot-common-security` provides JWT-based authentication and authorization integration for Spring Security applications. It standardizes the security filter chain, token extraction, user context access, and 401/403 response handling.

## 2. Main Components
- `com.mhd.boot.common.security.config.SecurityAutoConfiguration`
  - Registers password encoder, entry/denied handlers, token filter, and custom SecurityContext strategy
- `com.mhd.boot.common.security.config.SecurityConfig`
  - Builds the `SecurityFilterChain`
- `com.mhd.boot.common.security.core.filter.JwtAuthenticationTokenFilter`
  - Extracts token from request and, when valid, populates the authenticated user into the SecurityContext
- `com.mhd.boot.common.security.core.util.JwtUtils`
  - Generates, parses, and validates JWT tokens
- `com.mhd.boot.common.security.core.util.SecurityFrameworkUtils`
  - Reads and writes the current login user from SecurityContext
- `com.mhd.boot.common.security.core.handler.JwtAuthenticationEntryPoint`
  - Returns 401 responses for unauthenticated access
- `com.mhd.boot.common.security.core.handler.JwtAccessDeniedHandler`
  - Returns 403 responses for authenticated but unauthorized access
- `com.mhd.boot.common.security.core.LoginUser`
  - Login user payload used as the principal
- `com.mhd.boot.common.security.config.SecurityProperties`
  - JWT configuration such as header name, prefix, secret, expiration, and auth strategy

## 3. Boundaries and Non-Goals
This module does:
- handle authentication and authorization concerns for APIs
- provide unified user context access utilities
- support white-list or black-list style path strategies
- provide standard 401/403 handlers

This module does not:
- implement full OAuth2/OIDC flows
- manage session-based login state
- define business permissions or roles by itself
- act as a token revocation system

## 4. Runtime Behavior
1. Request enters the Spring Security filter chain
2. `JwtAuthenticationTokenFilter` extracts the token according to configured header/prefix rules
3. If the token is valid, `SecurityFrameworkUtils.setLoginUser(...)` stores the login user into the SecurityContext
4. Controller and service code can access the current user via `SecurityFrameworkUtils`
5. Unauthenticated or unauthorized access is converted to 401 or 403 by the handlers

## 5. Validation and Security Rules
- `SecurityProperties` uses `mhd.auth.jwt.*`
- `tokenHeader` defaults to `Authorization`
- `tokenStartWith` defaults to `Bearer `
- `authStrategy` can be `WHITE` or `BLACK`
- `SecurityConfig` disables CSRF and uses stateless sessions
- CORS is enabled through Spring Security defaults

## 6. Exception Model
- Unauthenticated access -> 401 via `JwtAuthenticationEntryPoint`
- Access denied -> 403 via `JwtAccessDeniedHandler`
- JWT parsing or validation failures -> runtime exceptions from `JwtUtils` or the token check implementation

## 7. Usage Guidance
Recommended:
- implement `TokenCheckService` in the business module when token validation is needed
- keep token claims compact and stable
- use `SecurityFrameworkUtils.getLoginUserId()` or `getLoginUsername()` when only simple identity data is required
- keep security rules in configuration rather than scattered controller checks

Be careful with:
- the token prefix when extracting Authorization headers
- large claim payloads, because they make tokens too large
- clock drift between client and server when token expiration is important

## 8. Minimal Example
```java
@Service
public class TokenCheckServiceImpl implements TokenCheckService {

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public boolean checkToken(String token) {
        return token != null && !JwtUtils.isExpired(token, securityProperties.getSecret());
    }

    @Override
    public LoginUser getLoginUser(String token) {
        Map<String, Object> info = JwtUtils.extractInfo(token, securityProperties.getSecret());
        LoginUser user = new LoginUser();
        user.setId(Long.valueOf(String.valueOf(info.get("userId"))));
        user.setUsername(String.valueOf(info.get("userName")));
        return user;
    }
}
```

## 9. Extended Example
```java
@GetMapping("/me")
public BaseResponse<Map<String, Object>> currentUser() {
    LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
    if (loginUser == null) {
        throw new BusinessException(ErrorCodeEnum.ERROR_400, "not logged in");
    }
    Map<String, Object> result = new HashMap<>();
    result.put("id", loginUser.getId());
    result.put("username", loginUser.getUsername());
    result.put("scopes", loginUser.getScopes());
    return BaseResultUtils.successOfData(result);
}
```

## 10. AI Reading Guidance
- Reuse `TokenCheckService` and the existing filter chain before adding any new security middleware.
- When the task is about current-user access, use `SecurityFrameworkUtils` instead of reading request attributes manually.
- If the task involves login failures, start with the existing 401/403 handlers rather than writing ad hoc JSON responses.

## 11. Testing Notes
- There are no module-specific tests in the repository for this module.
- If you change path strategy or token validation behavior, add tests for token extraction, filter chain behavior, and 401/403 responses.
