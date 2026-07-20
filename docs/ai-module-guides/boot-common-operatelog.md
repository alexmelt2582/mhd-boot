# boot-common-operatelog

## 1. Module Purpose
`boot-common-operatelog` provides annotation-driven operation logging for business actions. It uses AOP to capture a method invocation, collect context, and hand the assembled record to a pluggable handler so the business code stays clean.

## 2. Main Components
- `com.mhd.boot.common.operatelog.annotation.OperateLog`
  - Declares business number, title, type, operator, and other log metadata
- `com.mhd.boot.common.operatelog.aspect.OperateLogAspect`
  - Intercepts annotated methods and orchestrates collection plus handling
- `com.mhd.boot.common.operatelog.service.AbstractOperateLogDefaultCollectorService`
  - Default collector template for request, response, and environment data
- `com.mhd.boot.common.operatelog.service.OperateLogDefaultHandlerServiceImpl`
  - Default async-friendly handler implementation
- `com.mhd.boot.common.operatelog.config.OperateLogAutoConfiguration`
  - Registers the aspect and default services

## 3. Boundaries and Non-Goals
This module does:
- record business operation events with a declarative annotation
- keep collection and handling separated
- provide a fallback implementation that can work without a custom handler

This module does not:
- replace application logs or tracing systems
- define a retention or compliance policy
- guarantee that every field in a request should be audited

## 4. Runtime Behavior
1. A method annotated with `@OperateLog` is invoked
2. `OperateLogAspect` builds a log context from the method arguments, return value, and runtime environment
3. The collector creates a structured log payload
4. The handler persists, publishes, or otherwise processes the payload
5. The business flow should continue even when log handling is slow or partially failing

## 5. Validation and Log Rules
- Log metadata should come from the annotation rather than from hardcoded strings in the aspect
- Collectors should tolerate missing request or response data
- The handler should avoid blocking the business thread for long periods
- Sensitive payloads should be excluded before the log record is emitted

## 6. Exception Model
- Collector failures should be visible, but they should not silently corrupt the log payload
- Handler failures should not break the core business operation when the implementation is designed for best-effort logging

## 7. Usage Guidance
Recommended:
- annotate business events that matter for audit, support, or incident diagnosis
- keep the annotation title aligned with the business action, not with the HTTP endpoint
- log identifiers, state changes, and outcome summaries instead of full object dumps
- push expensive persistence or remote dispatch work out of the request thread

Be careful with:
- tokens, passwords, and payment data
- high-volume methods that would generate excessive log traffic
- using the module for generic debug logging, because that dilutes the audit signal

## 8. Minimal Example
```java
@PostMapping("/orders/{id}/approve")
@OperateLog(businessNo = "#id", title = "Approve order", businessType = BusinessTypeEnum.UPDATE)
public BaseResponse<Void> approve(@PathVariable Long id) {
    orderService.approve(id);
    return BaseResultUtils.success();
}
```

## 9. Extended Example
```java
@PostMapping("/refunds/{refundId}/confirm")
@OperateLog(businessNo = "#refundId", title = "Confirm refund", businessType = BusinessTypeEnum.UPDATE)
public BaseResponse<Void> confirm(@PathVariable Long refundId, @RequestBody RefundConfirmRequest request) {
    refundService.confirm(refundId, request);
    return BaseResultUtils.successOfData(null);
}
```

## 10. AI Reading Guidance
- Reuse the existing aspect and collector/handler split before inventing a second audit pipeline.
- If the task is only about debug tracing, application logs may be enough and this module may be unnecessary.
- If a field is sensitive, remove it while collecting the record, not after persistence.

## 11. Testing Notes
- This module currently has no module-specific tests in the repository.
- If you change annotation parsing or payload assembly, add tests for aspect interception, collector output, and handler invocation.
