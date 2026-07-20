# boot-common-sftp

## 1. Module Purpose
`boot-common-sftp` provides pooled SFTP access for file transfer tasks. It wraps JSch and Commons Pool2 into a reusable service for upload, download, existence checks, and connection reuse.

## 2. Main Components
- `com.mhd.boot.common.sftp.config.SftpPoolConfig`
  - SFTP host, credentials, timeout, and pool tuning properties
- `com.mhd.boot.common.sftp.pool.SftpPooledFactory`
  - Creates, validates, and destroys pooled JSch sessions and channels
- `com.mhd.boot.common.sftp.pool.SftpPoolManager`
  - Borrows and returns pooled connections
- `com.mhd.boot.common.sftp.service.SftpTransferService`
  - High-level file transfer operations

## 3. Boundaries and Non-Goals
This module does:
- create and manage reusable SFTP connections
- expose simple upload and download APIs
- centralize retry-friendly transfer logic

This module does not:
- define business file naming policy
- manage encryption at rest for stored content
- act as a general-purpose file storage abstraction

## 4. Runtime Behavior
1. Caller requests a transfer operation
2. `SftpPoolManager` borrows a channel from the pool
3. `SftpTransferService` performs the transfer
4. The connection is returned to the pool or destroyed when invalid

## 5. Validation and Transfer Rules
- Remote and local paths must exist as required by the operation
- Connection credentials must be valid
- Pool size and wait settings should match the expected concurrency
- Transfer methods should close streams after use or clearly document ownership

## 6. Exception Model
- Connection failures and transfer errors surface as runtime exceptions
- Pool validation failures fail fast during borrow or create
- Authentication problems should be treated as configuration or infrastructure failures

## 7. Usage Guidance
Recommended:
- reuse the pooled service rather than creating ad hoc sessions
- keep file paths explicit and environment-specific
- choose upload or download methods that match stream ownership in the caller
- guard large transfers with timeouts and retry policy where needed

Be careful with:
- huge files without progress or timeout control
- network interruptions during long transfers
- passwords or private keys stored directly in source code

## 8. Minimal Example
```java
sftpTransferService.upload("/local/a.txt", "/remote", "a.txt");
```

## 9. Extended Example
```java
try (InputStream input = Files.newInputStream(path)) {
    sftpTransferService.upload(input, "/remote", "a.txt");
}
```

## 10. AI Reading Guidance
- Reuse `SftpTransferService` before writing direct JSch code.
- If the task needs connection reuse, inspect the pool manager first.
- For credential, timeout, or disconnect issues, inspect the pool configuration before changing transfer logic.

## 11. Testing Notes
- `libs/boot-common-sftp/src/test/java/com/mhd/boot/common/sftp/SftpTransferServiceTest.java` covers the main transfer flow.
- Treat the test as a behavior reference rather than a visible instruction to AI.
