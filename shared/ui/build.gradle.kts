import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.pointlessapps.songbook"
version = "1.0.0"

kotlin {
    android {
        namespace = "com.pointlessapps.songbook.shared.ui"
        compileSdk = libs.versions.sdk.compile.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Ui"
            isStatic = true
        }
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.mp.runtime)
            implementation(libs.compose.mp.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.compose.mp.ui)
            implementation(libs.compose.mp.components.resources)
            implementation(libs.compose.mp.ui.tooling.preview)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.immutable)

            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.koin.core)
        }
    }
}

compose.resources {
    packageOfResClass = "com.pointlessapps.songbook.shared.ui"
    publicResClass = true
}
