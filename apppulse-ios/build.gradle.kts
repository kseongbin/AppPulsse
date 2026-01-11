plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "AppPulse"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":apppulse-core"))
        }
        iosTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
