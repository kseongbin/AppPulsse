# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AppPulse is a Jetpack Compose Android application built with Kotlin. The project uses modern Android development practices including Material3 design, edge-to-edge display support, and Gradle version catalogs for dependency management.

**Key Technologies:**
- Kotlin 2.0.21
- Jetpack Compose with Compose BOM 2024.09.00
- Material3 for UI components
- Min SDK 26, Target SDK 36, Compile SDK 36
- JVM Target 11

## Build Commands

**Build the project:**
```bash
./gradlew build
```

**Build specific variants:**
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

**Clean build:**
```bash
./gradlew clean
```

**Run lint checks:**
```bash
./gradlew lint
```

## Testing

**Run all unit tests:**
```bash
./gradlew test
```

**Run a specific unit test:**
```bash
./gradlew test --tests "com.example.apppulse.ClassName.testMethodName"
```

**Run all unit tests in a class:**
```bash
./gradlew test --tests "com.example.apppulse.ClassName"
```

**Run instrumented tests (requires connected device or emulator):**
```bash
./gradlew connectedAndroidTest
```

**Run a specific instrumented test:**
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.apppulse.ClassName#testMethodName
```

**Test locations:**
- Unit tests: `app/src/test/java/com/example/apppulse/`
- Instrumented tests: `app/src/androidTest/java/com/example/apppulse/`

## Architecture

**Current Structure:**
- Single-module application with package `com.example.apppulse`
- Single Activity architecture using Jetpack Compose
- MainActivity is the app entry point with edge-to-edge support
- UI theme system located in `ui/theme/` (Color.kt, Type.kt, Theme.kt)

**Composable Organization:**
- Composable functions follow Compose naming conventions (PascalCase)
- Preview functions annotated with `@Preview` for design-time rendering
- Modifiers passed as parameters following Compose best practices

## Dependency Management

Dependencies are managed through Gradle version catalog in `gradle/libs.versions.toml`.

**To add a new dependency:**
1. Add the version to `[versions]` section
2. Add the library to `[libraries]` section
3. Reference in `app/build.gradle.kts` using `implementation(libs.library.name)`

**To update dependency versions:**
- Modify versions in `gradle/libs.versions.toml`
- Compose BOM version controls all Compose library versions

## Code Conventions

**Kotlin:**
- Code style: Official Kotlin style (configured in `gradle.properties`)
- Package structure: `com.example.apppulse` for main code
- JVM target: Java 11

**Compose:**
- Use Material3 components from `androidx.compose.material3`
- Theme via `AppPulseTheme` wrapper
- Enable edge-to-edge with `enableEdgeToEdge()` in activities
- Use `Scaffold` for screen-level layouts with proper `innerPadding`

**Resources:**
- AndroidX enabled project-wide
- Non-transitive R classes enabled for build optimization
