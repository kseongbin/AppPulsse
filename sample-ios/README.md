# Sample iOS Integration

This directory documents how to integrate the Kotlin Multiplatform AppPulse SDK inside an iOS application. The recommended approach is:

1. Use Gradle to build the `:apppulse-ios` framework:
   ```bash
   ./gradlew :apppulse-ios:assemble
   ```
2. Drag the generated `AppPulse.framework` from `apppulse-ios/build/bin/ios*/releaseFramework/` into your Xcode project.
3. During `application(_:didFinishLaunchingWithOptions:)` initialize AppPulse:
   ```swift
   AppPulse.shared.init(config: AppPulseConfig(...), transport: MyTransport())
   IOSAppStartCollector().start()
   ```
4. Optionally wire `NetworkInstrumentation` to your `URLSession` delegate and `IOSFrameCollector` during startup to capture frame summaries around the first screen render.

A future iteration can replace this README with a runnable Xcode sample project.
