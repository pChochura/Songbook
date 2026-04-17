import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

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

    val props = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "GEMINI_API_KEY",
            props.getProperty("GEMINI_API_KEY"),
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "OLLAMA_API_KEY",
            props.getProperty("OLLAMA_API_KEY"),
        )
    }
}
