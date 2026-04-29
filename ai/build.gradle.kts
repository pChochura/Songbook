import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
}

kotlin {
    android {
        namespace = "com.pointlessapps.songbook.ai"
        compileSdk = libs.versions.sdk.compile.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.ktor)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.annotation)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

buildkonfig {
    packageName = "com.pointlessapps.songbook.ai"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "GEMINI_API_KEY",
            value = System.getenv("GEMINI_API_KEY") ?: project.findProperty("GEMINI_API_KEY")
                ?.toString(),
            nullable = true,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "OLLAMA_API_KEY",
            value = System.getenv("OLLAMA_API_KEY") ?: project.findProperty("OLLAMA_API_KEY")
                ?.toString(),
            nullable = true,
        )
    }
}
