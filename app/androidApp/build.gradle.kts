import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.awakeReleaseGuard)
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
            // libawake-vulkan ships for arm64-v8a and x86_64; filtering ABIs also excludes
            // 4 KB-aligned riscv64 third-party .so binaries that fail Android 15+ 16 KB checks.
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

val releaseGuardAllowDebugSigning = providers.gradleProperty("releaseGuard.allowDebugSigning")
    .map(String::toBoolean)
    .orElse(false)
    .get()

releaseGuard {
    projectPackages.add("com.awakekt.awake.template")
    minScore.set(80)
    failOnSeverity.set("HIGH")
    if (releaseGuardAllowDebugSigning) {
        disabledChecks.add("android.signing.release_cert")
    }

    android {
        enabled.set(true)
        autoConfigureReleaseHardening.set(true)
        artifactFile.set(layout.buildDirectory.file("outputs/apk/release/androidApp-release.apk"))
        // JoltJNI must keep its C++ JNI wrapper class/method names via META-INF/proguard;
        // maxKeptProjectClasses strictly guards that the app's own package stays flattened.
        maxReadability.set(0.25)
        minTargetSdkVersion.set(libs.versions.android.targetSdk.get().toInt())
        forbidDebuggable.set(true)
        forbidAllowBackup.set(true)
        forbidCleartextTraffic.set(true)
        forbidTestOnly.set(true)
        forbidExportedComponentsWithoutPermission.set(true)
        forbidWeakNetworkSecurityConfig.set(true)
        forbidDebugSigningCert.set(!releaseGuardAllowDebugSigning)
        requireV2OrV3Signing.set(!releaseGuardAllowDebugSigning)
        inspectNativeElfLibraries.set(true)
        requireElfHardening.set(true)
        requireElf16KbAlignment.set(true)
        forbidSensitiveBundledFiles.set(true)
    }

    supplyChain {
        enabled.set(true)
        forbidSnapshotDependencies.set(true)
    }
}

// AGP 9's lint-vital model tasks also read generated ProGuard files alongside R8 minify/merge.
tasks.matching { "Lint" in it.name || "lint" in it.name }.configureEach {
    dependsOn("generateReleaseGuardRules")
}

tasks.named("verifyReleaseGuard") {
    dependsOn("assembleRelease")
}
