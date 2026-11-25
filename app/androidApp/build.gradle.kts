import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":app:shared"))
    implementation(libs.awake.backend.vulkan)
    implementation(libs.awake.engine.window)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.awakekt.awake.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.awakekt.awake.template"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            // Shrunk and obfuscated by R8. Awake's libraries bring the rules for what their native
            // code finds by name; add your own to proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
            // libawake-vulkan ships for arm64-v8a and x86_64; filtering ABIs shrinks the prebuilt
            // android-player.apk (~223 MB -> ~48 MB) and excludes 4 KB-aligned third-party .so
            // binaries that fail Android 15+ 16 KB page-size checks.
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
            // Signed with the debug key so the release installs as it is, as CI installs it.
            // Sign it with your own key before you publish.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
