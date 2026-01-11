# Repository Guidelines

## Project Structure & Module Organization
- `app/` hosts the Android sample app, with Compose UI under `src/main/java` and resources in `src/main/res`. JVM unit tests live in `src/test`, and instrumentation tests live in `src/androidTest`.
- `apppulse-sdk/` is the Kotlin Multiplatform SDK. Shared logic is in `src/commonMain`, Android-specific code under `src/androidMain`, iOS in `src/iosMain`, and shared tests in `src/commonTest`.
- Root-level Gradle files (`build.gradle.kts`, `settings.gradle.kts`, `gradle/`) orchestrate dependencies and plugin versions.

## Build, Test, and Development Commands
- `./gradlew :app:assembleDebug` builds the sample app and validates Android-specific wiring.
- `./gradlew :app:testDebugUnitTest` runs JVM unit tests located in `app/src/test`.
- `./gradlew :app:connectedDebugAndroidTest` executes instrumentation tests on an attached emulator/device.
- `./gradlew :apppulse-sdk:publishToMavenLocal` packages the SDK for local consumption when iterating on client apps.
- `./gradlew lint ktlintCheck` (if configured) should run before commits to ensure formatting and static analysis.

## Coding Style & Naming Conventions
- Kotlin sources use 4-space indentation, trailing commas where helpful, and expression-bodied functions for simple mappings.
- Prefer descriptive, camelCase names for functions/variables, PascalCase for classes and composables, and snake_case for resource identifiers.
- Keep files focused: SDK features belong in `apppulse-sdk`, UI demos and wiring stay in `app`.
- Use Android Studio/IntelliJ’s “Reformat Code” with Kotlin style; run `ktlint` for consistency if available.

## Testing Guidelines
- Mirror production packages inside `app/src/test` and `apppulse-sdk/src/commonTest` to keep mocks scoped.
- Name tests after the behavior under test (`ClassName_function_underCondition_returnsExpectation`).
- For instrumentation, cover end-to-end flows that rely on Android services or sensors; unit tests should mock SDK interfaces.
- Target at least basic coverage for new SDK modules and update snapshots/golden data checked into `app/src/test/resources` when behavior changes.

## Commit & Pull Request Guidelines
- Write imperative, scope-aware commit messages under 72 characters (e.g., “Add session sampler to SDK”). Reference tickets like `APP-123` when relevant.
- Each PR should include a summary of changes, test evidence (`./gradlew test` output and screenshots for UI tweaks), and call out affected modules.
- Request review from maintainers of touched modules (`app` vs `apppulse-sdk`) and ensure CI passes before merging.

## Configuration & Security Tips
- Secrets (API keys, signing configs) belong in `local.properties` or encrypted Gradle properties—never commit them.
- Validate that debuggable builds strip sensitive logging by keeping SDK log levels behind build-time flags.
