import org.jetbrains.compose.internal.utils.getLocalProperty

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.pointlessapps.songbook"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "com.pointlessapps.songbook"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(getLocalProperty("storeFile")!!)
            storePassword = getLocalProperty("storePassword")!!
            keyAlias = getLocalProperty("keyAlias")!!
            keyPassword = getLocalProperty("keyPassword")!!
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.camera") {
                useVersion("1.4.0")
                because("CameraX 1.4.0+ aligns native libs for 16 KB page-size compatibility.")
            }
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.splashscreen)
    implementation(libs.koin.android)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    debugImplementation(libs.compose.mp.ui.tooling)
}
