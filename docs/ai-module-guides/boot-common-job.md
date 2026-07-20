# boot-common-job

## 1. Module Purpose
`boot-common-job` provides Quartz-based scheduling support for recurring and deferred tasks. It standardizes scheduler setup, job invocation, job logging, and the registration flow for job handlers.

## 2. Main Components
- `com.mhd.boot.common.job.config.MeQuartzAutoConfiguration`
  - Registers Quartz infrastructure and the default job wiring
- `com.mhd.boot.common.job.core.scheduler.SchedulerManager`
  - Encapsulates scheduler operations such as add, pause, resume, and delete
- `com.mhd.boot.common.job.core.invoker.JobHandlerInvoker`
  - Resolves and invokes handler methods by job metadata
- `com.mhd.boot.common.job.core.handler.JobLogHandlerService`
  - Persists or forwards execution logs
- `com.mhd.boot.common.job.core.annotation.JobHandler`
  - Declares a method as a named job handler

## 3. Boundaries and Non-Goals
This module does:
- manage Quartz lifecycle and trigger registration
- map handler names to executable methods
- expose a log hook for execution history

This module does not:
- decide business timing rules
- replace workflow engines or message queues
- guarantee exactly-once execution across all failure modes

## 4. Runtime Behavior
1. Application starts and initializes Quartz
2. `SchedulerManager` adds or updates a job definition
3. Quartz fires a trigger when the schedule matches
4. `JobHandlerInvoker` resolves the target method and calls it
5. `JobLogHandlerService` receives success or failure details

## 5. Validation and Scheduling Rules
- Job keys should be stable and unique
- Cron expressions should be validated before they are stored
- Job payload should include only the data needed by execution
- Long-running or remote calls should be time-bounded inside the job body

## 6. Exception Model
- Missing handler or invalid trigger configuration should fail fast
- Job execution failures should be recorded and surfaced to the logging layer
- Scheduler configuration errors should be treated as startup or admin-operation failures

## 7. Usage Guidance
Recommended:
- keep job handlers small and delegate business work to services
- validate cron expressions before persisting them
- keep the payload shape compact and versioned if needed
- use logs for observability, not for business decisions

Be careful with:
- duplicate job keys
- very long-running jobs that monopolize scheduler threads
- network calls without timeout control

## 8. Minimal Example
```java
@JobHandler(name = "syncUserJob")
public void syncUserJob(String payload) {
    userSyncService.sync(payload);
}
```

## 9. Extended Example
```java
public void scheduleMonthlyReport(String cron) {
    schedulerManager.addJob("monthlyReport", cron, Map.of("tenantId", 1L));
}
```

## 10. AI Reading Guidance
- Reuse `SchedulerManager` and the handler invocation chain before adding a new job abstraction.
- If the task only needs one-off delayed execution, Quartz may be heavier than necessary.
- When a job fails unexpectedly, verify handler resolution and cron parsing first.

## 11. Testing Notes
- `libs/boot-common-job/src/test/java/com/mhd/boot/common/job/QuartzControllerTest.java` and `TestJob` show example scheduling and handler usage.
- Treat them as behavior references, not as required pre-read instructions.
