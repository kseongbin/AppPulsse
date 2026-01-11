# AppPulse SDK 개요

AppPulse는 Android와 iOS에서 공용으로 사용할 수 있는 Kotlin Multiplatform 기반의 성능 측정 SDK 뼈대입니다. 앱에 부담을 주지 않는 가벼운 수집기를 목표로 하며, 기본 제공 모듈은 다음과 같습니다.

- `:apppulse-core` – 공용 API(AppPulse), 데이터 모델(Event/Span/Metric/Attributes), 전송/큐 인터페이스, 샘플러·레이트리미터·재시도 정책, 코루틴 기반 배치 워커를 포함합니다.
- `:apppulse-android` – Application/Activity 생명주기를 이용한 앱 시작 수집기, 선택적 프레임 메트릭 수집기(API 24+), OkHttp 네트워크 인터셉터 스켈레톤을 제공합니다.
- `:apppulse-ios` – iOS 앱 시작, URLSession 네트워크 래퍼, CADisplayLink 기반 프레임 요약 스켈레톤을 Framework로 내보냅니다.
- `:sample-android` – Compose 기반 데모 앱으로 SDK 초기화와 이벤트 트래킹 흐름을 확인할 수 있습니다.
- `:sample-ios` – Gradle에서 생성한 Framework를 Xcode 프로젝트에 붙이는 방법을 문서로 안내합니다.

## 빌드 및 테스트

```bash
./gradlew :apppulse-core:assemble
./gradlew :apppulse-android:assembleDebug
./gradlew :sample-android:installDebug
./gradlew :apppulse-ios:assemble
```

공용 로직 테스트는 `./gradlew :apppulse-core:check` 로 실행합니다. (로컬 환경에 Xcode/Command Line Tools가 설치되어 있어야 iOS 타깃 컴파일이 완료됩니다.)

## Android 연동 방법

1. `Application`에서 AppPulse 초기화:
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
2. 수집기 등록:
   ```kotlin
   AppStartCollector(this).register()
   FrameMetricsCollector(this, enabled = true).register()
   ```
3. OkHttp를 사용할 경우 `NetworkInterceptor`를 클라이언트에 추가합니다.
4. 원하는 위치에서 사용자 정의 이벤트를 기록합니다.
   ```kotlin
   AppPulse.trackEvent(
       type = EventType.CUSTOM,
       attributes = Attributes(mapOf("action" to "purchase"))
   )
   ```
5. 필요 시 수동 플러시를 호출합니다: `AppPulse.flush()`

## iOS 연동 방법

1. `./gradlew :apppulse-ios:assemble` 로 Framework를 빌드합니다.
2. `apppulse-ios/build/bin/ios*/releaseFramework/AppPulse.framework` 를 Xcode 프로젝트에 추가합니다.
3. `AppDelegate` 에서 초기화 및 수집기를 연결합니다.
   ```swift
   let config = AppPulseConfig(apiKey: "demo", endpoint: "https://collector.example.com")
   AppPulse.shared.init(config: config, transport: IOSConsoleTransport())
   IOSAppStartCollector().start()
   IOSFrameCollector().start()
   ```
4. 네트워크 계측이 필요하면 `NetworkInstrumentation.instrument(session:)` 으로 `URLSession` 에 훅을 달 수 있습니다.

## 공개 API 요약

| 함수 | 설명 |
| --- | --- |
| `AppPulse.init(config, transport, …)` | SDK 구성 및 배치 워커 시작 |
| `AppPulse.setUserId(id)` | 사용자 식별자 연결 |
| `AppPulse.trackEvent(type, attributes, metric)` | 샘플링/레이트리밋을 거쳐 이벤트 큐에 저장 |
| `AppPulse.startSpan(name)` / `endSpan(id)` | 수동 Span 계측 |
| `AppPulse.flush()` | 주기와 관계없이 즉시 배치 전송 |
| `AppPulse.queueSize()` | 현재 큐에 대기 중인 이벤트 수 확인 |

확장 가능한 인터페이스:
- `Transport` – 서버 전송 로직 구현
- `EventQueue` – 영속 큐 등 사용자 정의 큐 적용
- `Sampler`, `RateLimiter`, `RetryPolicy` – 맞춤 정책 주입
- `AppPulseClock`, `UuidGenerator` – 테스트 친화적 시간/ID 생성기

## 기본 정책

- 앱 시작/첫 화면: 100% 수집
- 네트워크: 기본 10% 샘플링 (설정 가능)
- 프레임: 앱 시작 구간 요약 (설정으로 On/Off)
- 세션당 최대 200 이벤트, 초과 시 새 이벤트를 버림
- 큐 용량 1000개, 가득 차면 가장 오래된 항목 삭제
- 배치 조건: 20개 혹은 10초마다 전송
- 재시도: 지수 백오프(1초 → 2초 → 4초, 최대 15초)

## 개인정보/안전 원칙

- 모든 수집기/워커는 예외를 내부에서 처리하여 앱 크래시를 일으키지 않습니다.
- URL/HTTP 메서드/지연 시간 등 최소 메타데이터만 기록하며, 민감한 헤더/바디는 취급하지 않습니다.
- 전송 계층(Transport)을 직접 구현해 암호화·마스킹·TLS 정책을 제어할 수 있습니다.

## 샘플 앱

- Android 데모(`:sample-android`)는 `AppStartCollector`가 앱 시작 이벤트를 즉시 큐에 넣는 과정을 logcat으로 보여줍니다.
- iOS 데모(`sample-ios/README.md`)는 Framework 빌드/연동 가이드를 제공하며 추후 실제 Xcode 프로젝트로 대체 가능합니다.

## 다음 단계 아이디어

- `EventQueue` 인터페이스를 활용해 파일/SQLDelight 기반 영속 큐를 도입합니다.
- Ktor/NSURLSession 기반 Transport 구현으로 실제 업로드 경로를 추가합니다.
- Android/iOS 수집기에 대한 통합 테스트와 계측 로직을 확장합니다.
