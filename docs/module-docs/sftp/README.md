# boot-common-sftp

## 1. Module Purpose
`boot-common-sftp` provides a reusable SFTP capability based on:
- JSch for SSH/SFTP protocol operations
- Apache Commons Pool2 for pooled `ChannelSftp` connections

The module is designed for high-frequency file operations in reporting/transfer jobs, with connection reuse, retry support, and structured error classification.

## 2. Main Components
- `config/SftpPoolConfig`
  - Defines all SFTP + pool parameters
  - Provides `valid()` for preflight validation
  - Provides `buildStartupConfigSummary()` for startup visibility
- `factory/SftpPooledFactory`
  - Creates, validates, and destroys `ChannelSftp`
  - Includes auth-failure circuit breaker to avoid aggressive invalid retries
- `pool/SftpPoolManager`
  - Owns `GenericObjectPool<ChannelSftp>` lifecycle
  - Borrow/return/invalidate operations
  - Exposes startup config summary for upper-layer service logging
- `service/SftpTransferService`
  - High-level file operations: upload/download/delete/list/mkdir/rename/recursive delete
  - Includes retry logic for retryable failures
  - Logs full startup configuration (pool + connection + retry)
- `service/SftpTransferServiceBuilder`
  - Fluent builder for required/optional parameters
  - Calls config validation before building service
- `exception/SftpTransferException`
  - Domain exception with stable error code taxonomy

## 3. Boundaries and Non-Goals
This module does:
- SFTP transport and remote file operations
- Connection pooling and connection health checks
- Retry orchestration for transient failures

This module does not:
- Manage business workflow/state persistence
- Provide transaction semantics across multiple file operations
- Perform content parsing, decryption, or domain-level validation
- Rotate credentials automatically

## 4. Startup and Runtime Behavior
1. Caller builds config via `SftpTransferServiceBuilder`
2. `SftpPoolConfig.valid()` validates required fields and numeric constraints
3. `SftpPoolManager` initializes pool
4. `SftpTransferService` constructor logs `buildStartupConfigSummary()` plus retry settings (`maxRetries`, `retryIntervalMillis`)
5. Runtime operations borrow channel from pool and always return/invalidate

Startup log summary clearly shows:
- target host and port
- pool settings and connection settings
- retry settings (`maxRetries`, `retryIntervalMillis`)
- whether each configurable field is currently default/custom
- masked password (never plain text)

## 5. Configuration Validation Rules
`SftpPoolConfig.valid()` enforces:
- required strings: `host`, `username`, `password` must be non-blank
- `port` in range `[1, 65535]`
- positive values: `timeout`, `maxTotal`, `timeBetweenEvictionRunsMillis`, `maxWaitMillis`, `serverAliveInterval`
- non-negative values: `maxIdle`, `minIdle`, `minEvictableIdleTimeMillis`, `serverAliveCountMax`
- pool relation constraints:
  - `maxIdle <= maxTotal`
  - `minIdle <= maxIdle`

## 6. Exception Model
Public API throws `SftpTransferException` with `errorCode`:
- `AUTH_FAILED`
- `CONNECTION_FAILED`
- `TRANSFER_FAILED`
- `PATH_ERROR`
- `PERMISSION_DENIED`
- `POOL_EXHAUSTED`
- `FILE_NOT_FOUND`
- `FILE_SIZE_EXCEEDED`

`fromJSchException(...)` maps low-level exceptions to these codes.

## 7. Retry Strategy
`SftpTransferService` retries only retryable failures.

Not retried:
- `FILE_NOT_FOUND`
- `FILE_SIZE_EXCEEDED`
- `PATH_ERROR`
- `PERMISSION_DENIED`
- `AUTH_FAILED`

Retried:
- `CONNECTION_FAILED`
- `POOL_EXHAUSTED`
- selected transient `TRANSFER_FAILED` cases (message keywords: timeout/socket/connection/channel/session)

## 8. Usage Guidance
Recommended:
- Create one service instance per target endpoint and reuse it
- Keep pool sizes aligned with remote server limits
- Call `close()` on service shutdown
- Use absolute remote paths where possible

Be careful with:
- very large files (module enforces max file size)
- retry settings too aggressive for unstable networks
- concurrent recursive delete on shared directories
- disabled host key checking (current implementation uses `StrictHostKeyChecking=no`)

## 9. Security Notes
- Password is masked in startup summary logs
- Host key checking is currently disabled for compatibility; this is less strict than production-grade SSH trust validation
- If stronger security is required, enhance `SftpPooledFactory` to manage known hosts / key verification

## 10. Concurrency Notes for Auth Breaker
- Auth breaker state uses a single volatile timestamp (`authBlockedUntilTimestamp`)
- `create()` reads this timestamp once per call and fails fast when still blocked
- `resetAuthBreaker()` sets timestamp to `0` and is safe under concurrent calls
- This avoids split-state races from multiple breaker fields (e.g., flag + timestamp)

## 11. Long-Run Operational Considerations
For multi-month production runtime, this module is generally stable if shutdown and capacity are managed correctly. Typical risks and mitigations:

- Risk: remote endpoint/network flapping can create repeated retries and noisy logs
  - Mitigation: tune `maxRetries` and `retryIntervalMillis` to your network profile
- Risk: pool sizing mismatch with remote limits can cause `POOL_EXHAUSTED` or server-side throttling
  - Mitigation: size `maxTotal/maxIdle/minIdle` based on actual server session limits
- Risk: long idle periods can still hit stale sessions depending on network devices
  - Mitigation: keep `testWhileIdle` enabled and configure eviction/keepalive values conservatively
- Risk: strict host trust is not enforced by default (`StrictHostKeyChecking=no`)
  - Mitigation: enable known-host verification for production security baselines
- Risk: service not closed during app shutdown can delay resource release
  - Mitigation: always call `SftpTransferService.close()` in shutdown hooks

## 12. Minimal Example
```java
SftpTransferService service = new SftpTransferServiceBuilder()
        .host("10.10.10.1")
        .port(22)
        .username("report_user")
        .password("***")
        .maxTotal(20)
        .minIdle(2)
        .maxRetries(2)
        .retryIntervalMillis(1000)
        .build();

service.upload("D:/tmp/report.csv", "/upload/daily", "report.csv");
service.close();
```
