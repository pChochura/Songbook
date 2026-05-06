import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildKonfig)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.pointlessapps.songbook.core"
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

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.mp.runtime)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.prefs)

            implementation(libs.paging.common)

            implementation(projects.core.supabase)
        }

        val nonWasmMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.room.runtime)
                implementation(libs.room.paging)
                implementation(libs.sqlite.bundled)
            }
        }

        androidMain.get().dependsOn(nonWasmMain)
        iosMain.get().dependsOn(nonWasmMain)

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.work.runtime)
            implementation(libs.androidx.credentials.core)
            implementation(libs.androidx.credentials.play.auth)
            implementation(libs.google.id.token)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

buildkonfig {
    packageName = "com.pointlessapps.songbook.core"

    defaultConfigs {
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "GOOGLE_WEB_CLIENT_ID",
            value = System.getenv("GOOGLE_WEB_CLIENT_ID")
                ?: getLocalProperty("GOOGLE_WEB_CLIENT_ID"),
            nullable = true,
        )
        buildConfigField(
            type = FieldSpec.Type.STRING,
            name = "REMOVE_ACCOUNT_WEBSITE_URL",
            value = System.getenv("REMOVE_ACCOUNT_WEBSITE_URL")
                ?: getLocalProperty("REMOVE_ACCOUNT_WEBSITE_URL"),
            nullable = true,
        )
    }
}
