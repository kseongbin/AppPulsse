# AppPulse SDK Skeleton

[한국어 버전 README_ko.md](README_ko.md)

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

## Gradle Dependency (JitPack)

If you prefer to consume the SDK as an external dependency, publish tags on GitHub and add the JitPack repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then depend on the modules you need (Android apps typically only need `apppulse-android`):

```kotlin
dependencies {
    implementation("com.github.kseongbin:apppulse-core:<version>")
    implementation("com.github.kseongbin:apppulse-android:<version>")
}
```

Replace `<version>` with a tagged release such as `0.1.0` or a specific commit hash recognized by JitPack.

## Getting Started (Android)

1. Initialize AppPulse inside your `Application` class and gate it behind a boolean so you can toggle with build flavors or remote config:
   ```kotlin
   class SampleApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           val config = AppPulseConfig(
               apiKey = "demo-key",
               endpoint = "https://collector.example.com"
           )
           val isAppPulseEnabled = !BuildConfig.DEBUG
           AppPulse.init(config, transport = MyTransport(), enabled = isAppPulseEnabled)
           if (isAppPulseEnabled) {
               AppPulse.setUserId("user-123")
           }
       }
   }
   ```
2. Register collectors only when `isAppPulseEnabled` is true:
   ```kotlin
   if (isAppPulseEnabled) {
       AppStartCollector(this).register()
       FrameMetricsCollector(this, enabled = true).register()
   }
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
3. In `AppDelegate`, initialize and wire collectors with the same boolean flag so each app can decide whether AppPulse should run:
   ```swift
   let config = AppPulseConfig(apiKey: "demo", endpoint: "https://collector.example.com")
   let isAppPulseEnabled = RemoteConfig.shared.isAppPulseEnabled
   AppPulse.shared.init(config: config, transport: IOSConsoleTransport(), enabled: isAppPulseEnabled)
   if isAppPulseEnabled {
       IOSAppStartCollector().start()
       IOSFrameCollector().start()
   }
   ```
4. Wrap `URLSession` via `NetworkInstrumentation.instrument(session:)` to capture networking metrics.

## Public API Summary

| Function | Purpose |
| --- | --- |
| `AppPulse.init(config, transport, …, enabled)` | Configure SDK and background worker (`enabled = false` short-circuits setup). |
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
