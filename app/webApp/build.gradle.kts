import com.awakekt.releaseguard.model.ReleaseGuardPolicy
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.awakeReleaseGuard)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":app:shared"))

            implementation(libs.compose.ui)
        }
        wasmJsMain.dependencies {
            implementation(libs.awake.backend.webgpu)
        }
        // The browser fetches the project next to index.html, so the web app serves the shared one.
        wasmJsMain {
            resources.srcDir(rootProject.file("app/shared/src/commonMain/resources"))
        }
    }
}

releaseGuard {
    projectPackages.add("com.awakekt.awake.template")
    allowedLibraryPathPatterns.set(
        ReleaseGuardPolicy.DEFAULT_ALLOWED_LIBRARY_PATHS + listOf(
            // AndroidX Compose Multiplatform runtime CI paths embedded in prebuilt klibs.
            """/androidx/compose/""",
        ),
    )
    minScore.set(80)
    failOnSeverity.set("HIGH")

    web {
        enabled.set(true)
        stripKclassFqn.set(true)
        inputDir.set(layout.buildDirectory.dir("dist/wasmJs/productionExecutable"))
        strippedOutputDir.set(layout.buildDirectory.dir("release-guard/web-hardened"))
        stripWasmDebugSections.set(true)
        dropSourceMaps.set(true)
        generateSecurityHeaders.set(true)
        requireSecurityHeaders.set(true)
        gradeCspQuality.set(true)
        allowWasmUnsafeEvalInCsp.set(true)
        requireSubresourceIntegrity.set(true)
        // Compose Multiplatform's Skiko WASM embeds Brotli static dictionary fragments like
        // "http://.css" and "http://www."; keep debugger-statement scanning enabled.
        forbidInsecureWebEndpoints.set(false)
        forbidDebuggerStatements.set(true)
        forbidSensitiveDistFiles.set(true)
    }
}

tasks.named("hardenWebBundle") {
    dependsOn("wasmJsBrowserDistribution")
}

tasks.named("verifyReleaseGuard") {
    dependsOn("hardenWebBundle")
}
