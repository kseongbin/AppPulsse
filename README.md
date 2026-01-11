# AppPulse SDK Skeleton

Lightweight Kotlin Multiplatform performance telemetry SDK that targets Android and iOS with shared core logic and optional platform collectors.

## Modules

- `:apppulse-core` – Shared SDK with public API (`AppPulse`), event models, queueing, sampling, retry, and coroutine-based batch worker.
- `:apppulse-android` – Android-specific collectors (app start, optional frame metrics, OkHttp network interceptor).
- `:apppulse-ios` – iOS helper framework exposing collectors implemented in `iosMain` and packaged as `AppPulse.framework`.
- `:sample-android` – Compose sample app wiring the SDK and demonstrating a launch metric being queued.
- `:sample-ios` – Documentation stub for integrating the framework into an Xcode project.

## Building

```bash
./gradlew :apppulse-core:assemble
./gradlew :apppulse-android:assembleDebug
./gradlew :sample-android:installDebug
./gradlew :apppulse-ios:assemble
```

Unit tests for the shared logic:

```bash
./gradlew :apppulse-core:check
```

## Getting Started (Android)

1. Initialize AppPulse inside your `Application` class:
   ```kotlin
   class SampleApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           val config = AppPulseConfig(
               apiKey = "demo-key",
               endpoint = "https://collector.example.com"
           )
           AppPulse.init(config, transport = MyTransport())
           AppPulse.setUserId("user-123")
       }
   }
   ```
2. Register collectors:
   ```kotlin
   AppStartCollector(this).register()
   FrameMetricsCollector(this, enabled = true).register()
   ```
3. Optionally add the `NetworkInterceptor` to your OkHttp client for network sampling.
4. Track custom events anywhere:
   ```kotlin
   AppPulse.trackEvent(
       type = EventType.CUSTOM,
       attributes = Attributes(mapOf("action" to "purchase"))
   )
   ```
5. Flush manually when needed:
   ```kotlin
   AppPulse.flush()
   ```

## Getting Started (iOS)

1. Build the framework: `./gradlew :apppulse-ios:assemble`.
2. Add the generated `AppPulse.framework` to Xcode.
3. In `AppDelegate`, initialize and wire collectors:
   ```swift
   let config = AppPulseConfig(apiKey: "demo", endpoint: "https://collector.example.com")
   AppPulse.shared.init(config: config, transport: IOSConsoleTransport())
   IOSAppStartCollector().start()
   IOSFrameCollector().start()
   ```
4. Wrap `URLSession` via `NetworkInstrumentation.instrument(session:)` to capture networking metrics.

## Public API Summary

| Function | Purpose |
| --- | --- |
| `AppPulse.init(config, transport, …)` | Configure SDK and background worker. |
| `AppPulse.setUserId(id)` | Associate metrics with a logical user. |
| `AppPulse.trackEvent(type, attributes, metric)` | Queue an event respecting sampling and rate limits. |
| `AppPulse.startSpan(name)` / `endSpan(id)` | Simple span API for manual instrumentation. |
| `AppPulse.flush()` | Force a batch upload outside of the periodic schedule. |

Core interfaces for extensibility:

- `Transport` – implement to ship batches to your backend (includes retry hints).
- `EventQueue` – plug in an alternative queue (e.g., SQLDelight) if you need persistence.
- `Sampler`, `RateLimiter`, `RetryPolicy` – override defaults for custom policies.
- `AppPulseClock` and `UuidGenerator` – provide deterministic versions for testing.

## Defaults & Policies

- App start & first screen metrics: sampled at 100%.
- Network metrics: 10% sampling (configurable via `networkSamplingRate`).
- Frame summaries: collected only during the initial window unless enabled.
- Max events per session: 200 (overflow silently drops new events).
- Queue capacity: 1000 with drop-oldest strategy.
- Batch flush: 20 events or every 10 seconds.
- Retry policy: exponential backoff (1s, 2s, 4s, capped at 15s).

## Privacy & Safety

- SDK never crashes the host app: every collector and worker call is wrapped in `runCatching`.
- Only sanitized metadata (URL, method, durations) is recorded by default; sensitive headers/body data is not touched.
- Provide your own `Transport` to control encryption, redaction, and TLS settings.

## Sample Apps

- Android sample (`:sample-android`) uses `AppStartCollector` so you can see a launch event enter the queue immediately in `logcat` (`AppStartCollector` logs queue size after enqueue).
- iOS sample instructions (`sample-ios/README.md`) explain how to build and embed the framework until a full Xcode project is added.

## Next Steps

- Swap `InMemoryEventQueue` with a persistence-backed implementation via the `EventQueue` interface.
- Implement platform-specific transports (Ktor, NSURLSession) using the provided skeletons.
- Expand tests to cover collectors once business rules are finalized.
