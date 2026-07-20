# boot-common-sse

## 1. Module Purpose
`boot-common-sse` provides server-sent events support for server-to-client push notifications and topic-based fan-out. It is designed for lightweight, one-way updates where the server streams incremental state to connected clients.

## 2. Main Components
- `com.mhd.boot.common.sse.config.SseAutoConfiguration`
  - Registers SSE infrastructure
- `com.mhd.boot.common.sse.manager.SseEmitterManager`
  - Tracks emitters and emitter lifecycle
- `com.mhd.boot.common.sse.listener.SseTopicListener`
  - Redis pub/sub listener for topic fan-out
- `com.mhd.boot.common.sse.utils.SseMessageUtils`
  - Builds message payloads and event metadata
- `com.mhd.boot.common.sse.controller.SseController`
  - Connection and message endpoints
- `com.mhd.boot.common.sse.domain.SseEventDto`
  - Event metadata DTO
- `com.mhd.boot.common.sse.domain.SseMessageDto`
  - Message DTO for payload delivery

## 3. Boundaries and Non-Goals
This module does:
- provide one-way push over SSE
- manage client emitters and cleanup
- fan out topic messages through Redis listener integration

This module does not:
- provide bidirectional realtime communication
- replace WebSocket when richer interaction is needed
- guarantee durable delivery or client acknowledgement

## 4. Runtime Behavior
1. Client opens an SSE connection
2. `SseEmitterManager` stores the emitter under a stable key
3. The server publishes an event directly or through Redis pub/sub
4. `SseTopicListener` forwards topic messages to matching clients
5. Disconnect, timeout, or send failure removes the emitter from the manager

## 5. Validation and Message Rules
- Event payloads should be serializable and compact
- Topic names should be stable and meaningful
- Emitter timeout should be configured to match client expectations
- The send path should tolerate client disconnect as a normal lifecycle event

## 6. Exception Model
- Broken pipe or client disconnect should be treated as lifecycle cleanup
- Message serialization failure should surface in the send path
- Redis pub/sub failure should be handled as infrastructure failure, not as a client error

## 7. Usage Guidance
Recommended:
- use SSE for low-frequency status updates, job progress, and light notifications
- keep payloads compact and JSON-friendly
- remove emitters on timeout and send failure
- use Redis fan-out when multiple app instances need the same push signal

Be careful with:
- high-frequency streaming workloads
- browser connection limits
- large payloads or binary payloads
- relying on SSE for critical delivery confirmation

## 8. Minimal Example
```java
sseEmitterManager.send(userId, "refresh", Map.of("status", "ok"));
```

## 9. Extended Example
```java
@GetMapping("/events")
public SseEmitter connect(@RequestParam Long userId) {
    return sseEmitterManager.create(userId);
}
```

## 10. AI Reading Guidance
- Reuse `SseEmitterManager` and the topic listener before adding a second push mechanism.
- If the task needs durable streams or acknowledgements, SSE may be the wrong fit.
- If a task mentions fan-out, inspect the Redis listener path before touching controller code.

## 11. Testing Notes
- `SseController.connect()` is currently a TODO in source, so connection behavior should be treated carefully during changes.
- There are no module-specific tests in the repository for this module.
- If you change emitter lifecycle or message format, add tests for connect, timeout, and send behavior.
