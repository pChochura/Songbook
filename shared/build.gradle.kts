import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

group = "com.pointlessapps.songbook"
version = "1.0.0"

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-XXLanguage:+ExpectRefinement")
    }

    android {
        namespace = "com.pointlessapps.songbook.shared"
        compileSdk = libs.versions.sdk.compile.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
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
        commonMain.dependencies {
            implementation(libs.compose.mp.runtime)
            implementation(libs.compose.mp.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.compose.mp.material.icons.extended)
            implementation(libs.compose.mp.ui)
            implementation(libs.compose.mp.components.resources)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.navigation)

            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.androidx.navigation3.ui)

            implementation(libs.peekaboo.ui)
            implementation(libs.peekaboo.image.picker)

            implementation(projects.ai)
        }
    }
}

compose.resources {
    packageOfResClass = "com.pointlessapps.songbook.shared"
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
