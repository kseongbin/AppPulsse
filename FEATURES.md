# AppPulse SDK 기능 안내

이 문서는 Kotlin Multiplatform 기반 AppPulse SDK가 제공하는 기능을 모듈별로 요약합니다.

## 공용 모듈 (:apppulse-core)

- **AppPulse API**: `init`, `setUserId`, `trackEvent`, `startSpan`/`endSpan`, `flush`, `queueSize` 등 SDK의 엔트리포인트를 제공합니다. `enabled` 플래그로 런타임 토글 가능.
- **데이터 모델**: `Event`, `Span`, `Metric`, `Attributes`, `EventType` 정의 및 직렬화.
- **샘플링/레이트리밋**: 앱 시작/첫 화면(100%), 네트워크(기본 10%), 프레임 요약 샘플링 비율과 세션당 200개 이벤트 제한.
- **큐/전송 인터페이스**: `EventQueue`, `Transport` 인터페이스와 인메모리 큐, 전송 실패 시 재시도 정책(`RetryPolicy`, `ExponentialBackoff` 기본 제공).
- **백그라운드 배치 워커**: 코루틴 기반 `BatchWorker`로 일정량 또는 간격마다 묶음 전송. 모든 작업은 `runCatching`으로 감싸 앱 크래시를 방지.
- **플랫폼 추상화**: `AppPulseClock`, `UuidGenerator`로 시간을 주입하거나 테스트 더블을 연결 가능.

## Android 모듈 (:apppulse-android)

- **AppStartCollector**: `Application`과 `ActivityLifecycleCallbacks`를 활용해 콜드/웜 스타트, 첫 화면 렌더 시간을 측정.
- **FrameMetricsCollector**: API 24+ 환경에서 `FrameMetrics`/`Choreographer` 기반 프레임 요약을 수집하고, 이하 버전은 자동으로 no-op.
- **NetworkInterceptor**: OkHttp 네트워크 요청의 왕복 시간·상태 코드를 `EventType.NETWORK`로 전송하도록 돕는 스켈레톤.
- **Transport 예시**: 샘플 앱에서 콘솔 전송기를 제공해 배치 전송 과정을 확인 가능.

## iOS 모듈 (:apppulse-ios)

- **AppStartCollector**: `UIApplication` 생명주기를 감지해 앱 시작/첫 화면 이벤트를 기록.
- **NetworkInstrumentation**: `URLSession`을 감싸 요청/응답 지표를 Event로 전송하는 스켈레톤.
- **FrameCollector**: `CADisplayLink` 기반으로 초기 구간의 프레임 요약을 수집하는 스켈레톤(기본 OFF).
- **프레임워크 패키징**: `AppPulse.framework`/`AppPulse.xcframework`를 빌드하여 Xcode에서 임포트 가능하며 GitHub Actions가 `AppPulse.xcframework.zip`을 자동 배포.

## 구성 옵션

- `apiKey`, `endpoint`: 서버 인증 및 전송 목적지.
- `batchSize`/`batchIntervalMs`: 배치 조건(기본 20개 또는 10초).
- `maxEventsPerSession`, `queueMaxSize`: 세션/전체 큐 용량 제한.
- `networkSamplingRate`, `frameSamplingRate`, `frameSummaryWindowMs`: 각 수집기 샘플링 비율 및 프레임 요약 구간.
- `debugLogging`: 샘플 앱/개발 환경에서 내부 로그 노출 여부.

## 확장 지점

- `Transport`: HTTP/Grpc 등 원하는 프로토콜로 업로드 구현.
- `EventQueue`: SQLDelight, 파일 기반 큐 등 영속 저장소로 교체 가능.
- `Sampler`, `RateLimiter`, `RetryPolicy`: 정책 커스터마이즈.
- `Clock`, `UuidGenerator`: 테스트·디버깅을 위한 결정적 구현 주입.

> 상세 초기화/코드 예시는 README/README_ko의 “Getting Started” 절을 참고하세요.
