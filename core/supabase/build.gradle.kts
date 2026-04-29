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
        namespace = "com.pointlessapps.songbook.core.supabase"
        compileSdk = libs.versions.sdk.compile.get().toInt()
        minSdk = libs.versions.sdk.min.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    )

    @Suppress("OPT_IN_USAGE")
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            api(libs.supabase.auth)
            api(libs.supabase.postgres)
            api(libs.supabase.realtime)
            api(libs.supabase.functions)
        }
    }
}

buildkonfig {
    packageName = "com.pointlessapps.songbook.core.supabase"

    val props = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "SUPABASE_URL",
            props.getProperty("SUPABASE_URL"),
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "SUPABASE_KEY",
            props.getProperty("SUPABASE_KEY"),
        )
    }
}
