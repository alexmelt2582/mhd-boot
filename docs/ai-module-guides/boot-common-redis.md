# boot-common-redis

## 1. Module Purpose
`boot-common-redis` provides Redis-based shared infrastructure for the repository. It wraps Redisson, Spring Cache, and local Caffeine caching into one reusable module for caching, rate limiting, pub/sub, atomic counters, and basic key/value operations.

## 2. Main Components
- `com.mhd.boot.common.redis.config.RedisConfig`
  - Configures Redisson codec, threads, script cache, and single/cluster server options
- `com.mhd.boot.common.redis.config.CacheConfig`
  - Enables Spring Cache and registers the custom cache manager
- `com.mhd.boot.common.redis.manager.PlusSpringCacheManager`
  - Parses cache name hints such as TTL, idle time, size, and local-cache flag
  - Chooses between `RMap` and `RMapCache`
- `com.mhd.boot.common.redis.utils.RedisUtils`
  - Static utility entry point for rate limiting, cache operations, pub/sub, lists, sets, maps, atomic counters, and key scans
- `com.mhd.boot.common.redis.handler.RedisExceptionHandler`
  - Translates lock failures into a stable user-facing response
- `com.mhd.boot.common.redis.handler.KeyPrefixHandler`
  - Applies key prefixing for Redisson names

## 3. Boundaries and Non-Goals
This module does:
- provide distributed caching and rate limiting primitives
- support a two-level cache pattern through local Caffeine plus Redis
- simplify common Redis operations used across services
- provide stable error translation for lock failures

This module does not:
- guarantee strong consistency for business transactions
- replace domain persistence logic
- define business-specific Redis key naming rules by itself
- hide all Redis operational concerns such as memory sizing and eviction policy

## 4. Runtime Behavior
1. Application starts and creates a Redisson client from `RedisConfig`
2. `CacheConfig` enables Spring Cache and exposes the custom manager
3. `RedisUtils` is used directly for imperative Redis operations
4. `PlusSpringCacheManager` resolves cache-name hints and builds the correct cache object
5. Lock failures are handled by `RedisExceptionHandler`

## 5. Validation and Cache Rules
`PlusSpringCacheManager` interprets cache name suffix hints separated by `#`:
- TTL
- max idle time
- max size
- local cache enable flag

`RedisUtils.rateLimiter(...)` returns:
- a non-negative permit count when the request is allowed
- `-1` when the request is rejected

`RedisConfig` currently:
- uses JSON-based codec settings
- enables Lua script cache
- supports single server and cluster server modes
- can switch to a virtual-thread executor when the runtime supports it

## 6. Exception Model
- `LockFailureException` -> translated to an availability-style response
- Redis connection or serialization failures -> runtime exceptions that should be handled at the service boundary

## 7. Usage Guidance
Recommended:
- keep Redis keys in constants or enums
- apply short TTLs for rate-limited or transient cache data
- use `RedisUtils` only where imperative Redis access is actually needed
- use `@Cacheable` with `PlusSpringCacheManager` for standard cache reads

Be careful with:
- large serialized values, because they increase memory and network cost
- keys that expire at the same time, because they can create a cache avalanche
- using local cache for data that changes too frequently, because the local layer can lag behind Redis briefly

## 8. Minimal Example
```java
long permits = RedisUtils.rateLimiter("order:rate:" + userId, RateType.OVERALL, 5, 1);
if (permits < 0) {
    return BaseResultUtils.error("429", "too many requests");
}

RedisUtils.setCacheObject("order:summary:" + orderId, summary, Duration.ofMinutes(10));
```

## 9. Extended Example
```java
@Cacheable(value = "userProfile#30m#0#0#1", key = "#userId")
public UserProfileDTO getProfile(Long userId) {
    UserProfileDTO profile = userMapper.selectProfile(userId);
    if (profile == null) {
        throw new BusinessException(ErrorCodeEnum.FAIL, "profile not found");
    }
    return profile;
}
```

## 10. AI Reading Guidance
- Reuse `RedisUtils` before inventing direct Redisson calls in service code.
- Reuse `PlusSpringCacheManager` cache hints instead of building a custom TTL parser.
- For lock failures, rely on the existing exception handler instead of inventing a second response strategy.

## 11. Testing Notes
- This module currently has no module-specific tests in the repository.
- If you change the cache manager rules, add tests for TTL parsing, local-cache enablement, and fallback behavior.
