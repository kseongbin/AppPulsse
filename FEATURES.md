# AppPulse SDK 기능 안내

Android/iOS 프로젝트에서 바로 사용할 수 있는 기능 목록과 호출 예시를 정리했습니다.

## Android 모듈 (:apppulse-android)

- **AppStartCollector** – Application/ActivityLifecycleCallbacks 기반으로 앱 시작 이벤트 및 첫 화면 렌더 시간을 측정합니다.
- **FrameMetricsCollector** – API 24+에서 프레임 드롭/시간을 요약합니다(이하 버전은 no-op).
- **NetworkInterceptor** – OkHttp 요청에 부착해 `EventType.NETWORK` 이벤트를 생성합니다.

```kotlin
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val enabled = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0
        val config = AppPulseConfig(apiKey = "demo", endpoint = "https://collector.example.com")
        AppPulse.init(config = config, transport = ConsoleTransport(), enabled = enabled)
        if (enabled) {
            AppStartCollector(this).register()
            FrameMetricsCollector(this, enabled = config.frameSamplingRate > 0).register()
        }
    }
}

Interceptor { chain ->
    val start = System.currentTimeMillis()
    val response = chain.proceed(chain.request())
    val duration = System.currentTimeMillis() - start
    AppPulse.trackEvent(
        EventType.NETWORK,
        Attributes(mapOf(
            "url" to chain.request().url.toString(),
            "status" to response.code.toString(),
            "durationMs" to duration.toString()
        ))
    )
    response
}
```

## iOS 모듈 (:apppulse-ios)

- **IOSAppStartCollector** – `UIApplication` 생명주기에서 앱 시작·첫 화면 이벤트를 기록합니다.
- **NetworkInstrumentation** – `URLSession` 인스턴스를 감싸 HTTP 요청 지표를 이벤트로 전송합니다.
- **IOSFrameCollector** – `CADisplayLink` 기반 프레임 요약을 수집합니다(기본 비활성).
- **AppPulse.xcframework** – GitHub Actions가 자동 빌드하는 프레임워크를 Xcode에 추가하면 됩니다.

```swift
import AppPulse

func application(_ application: UIApplication,
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    let config = AppPulseConfig(apiKey: "demo", endpoint: "https://collector.example.com")
    let enabled = RemoteConfig.shared.isAppPulseEnabled
    AppPulse.shared.init(config: config, transport: IOSConsoleTransport(), enabled: enabled)
    if enabled {
        IOSAppStartCollector().start()
        IOSFrameCollector().start()
        NetworkInstrumentation().instrument(session: URLSession.shared)
    }
    return true
}
```
