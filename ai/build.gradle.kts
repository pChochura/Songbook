import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProperties = Properties().also { props ->
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/source/buildConfig")
    val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "")
    val ollamaApiKey = localProperties.getProperty("OLLAMA_API_KEY", "")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "ApiKeys.kt").writeText(
            """
            package com.pointlessapps.songbook

            internal const val geminiApiKey: String = "$geminiApiKey"
            internal const val ollamaApiKey: String = "$ollamaApiKey"
            """.trimIndent(),
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>>().configureEach {
    dependsOn(generateBuildConfig)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-XXLanguage:+ExpectRefinement")
    }

    android {
        namespace = "com.pointlessapps.songbook.ai"
        compileSdk = libs.versions.sdk.compile.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/source/buildConfig"))
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.ktor)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
