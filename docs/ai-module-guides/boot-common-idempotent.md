# boot-common-idempotent

## 1. Module Purpose
`boot-common-idempotent` provides request de-duplication and idempotency control through an annotation-driven AOP approach. It supports Redis-backed and in-memory key stores.

## 2. Main Components
- `com.mhd.boot.common.idempotent.core.annotation.Idempotent`
  - Declares prefix, unique expression, TTL, message, and cleanup behavior
- `com.mhd.boot.common.idempotent.core.aspect.IdempotentAspect`
  - Generates the idempotent key, stores it if absent, executes the method, and cleans up when needed
- `com.mhd.boot.common.idempotent.core.key.generator.IdempotentKeyGenerator`
  - Key generation contract
- `com.mhd.boot.common.idempotent.core.key.generator.DefaultIdempotentKeyGenerator`
  - Default SpEL-based key generator
- `com.mhd.boot.common.idempotent.core.key.store.IdempotentKeyStore`
  - Key store contract
- `com.mhd.boot.common.idempotent.core.key.store.RedisIdempotentKeyStore`
  - Redis-backed key store using `setIfAbsent`
- `com.mhd.boot.common.idempotent.core.key.store.MemoryIdempotentKeyStore`
  - In-memory key store for simpler environments
- `com.mhd.boot.common.idempotent.config.IdempotentConfig`
  - Registers generator, store, and aspect

## 3. Boundaries and Non-Goals
This module does:
- block duplicate submissions inside a TTL window
- support retry-friendly cleanup policies
- support either Redis or in-memory storage

This module does not:
- provide distributed transaction guarantees
- replace business-level deduplication logic for every use case
- guarantee exactly-once delivery across external systems

## 4. Runtime Behavior
1. Method annotated with `@Idempotent` is intercepted by the aspect
2. The key generator computes a stable key from the request context
3. The key store attempts `saveIfAbsent`
4. If the key already exists, an `IdempotentException` is thrown
5. If the method finishes or errors, cleanup behavior is applied based on annotation flags

## 5. Validation and Key Rules
- `prefix` defaults to `idem`
- `uniqueExpression` uses SpEL and should uniquely identify the request
- `duration` and `timeUnit` define the TTL window
- `removeKeyWhenFinished` controls cleanup after success
- `removeKeyWhenError` controls cleanup after failure

## 6. Exception Model
- `IdempotentException` -> duplicate request rejected
- SpEL evaluation or key generation failures -> runtime errors that should be handled by the service boundary

## 7. Usage Guidance
Recommended:
- use a request identifier that is stable across retries, such as `userId + requestNo`
- choose Redis store for production multi-instance systems
- keep the TTL window aligned with the actual repeat-submission risk
- clean up keys on error only when the business flow allows retry

Be careful with:
- expressions that do not uniquely identify the request
- TTLs that are too short or too long for the workflow
- using the memory store in a horizontally scaled environment

## 8. Minimal Example
```java
@PostMapping("/orders")
@Idempotent(prefix = "order:create", uniqueExpression = "#req.userId + ':' + #req.requestNo",
        duration = 5, timeUnit = TimeUnit.SECONDS, removeKeyWhenError = true)
public BaseResponse<Long> createOrder(@RequestBody CreateOrderRequest req) {
    Long orderId = orderService.create(req);
    return BaseResultUtils.successOfData(orderId);
}
```

## 9. Extended Example
```java
@PostMapping("/payments/callback")
@Idempotent(prefix = "payment:callback", uniqueExpression = "#callback.tradeNo",
        duration = 60, timeUnit = TimeUnit.SECONDS, removeKeyWhenFinished = false,
        removeKeyWhenError = true)
public BaseResponse<Void> handleCallback(@RequestBody PaymentCallbackRequest callback) {
    paymentService.confirm(callback);
    return BaseResultUtils.success();
}
```

## 10. AI Reading Guidance
- For write operations that can be retried by clients, prefer `@Idempotent` instead of manually checking duplicate flags.
- When choosing the unique expression, follow the actual business identifier rather than a random payload hash unless the workflow truly requires it.
- If a task mentions Redis-backed idempotency, first verify the existing key-store selection in auto configuration.

## 11. Testing Notes
- `libs/boot-common-idempotent/src/test/java/com/mhd/boot/common/idempotent/DemoControllerTest.java` exercises concurrent duplicate submission behavior.
- Treat the test as a behavior reference for the aspect, not as a required pre-read before using the module.
