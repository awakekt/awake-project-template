import com.awakekt.releaseguard.gradle.CollectLibraryKeepRulesTask
import com.awakekt.releaseguard.gradle.GenerateReleaseGuardRulesTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.awakeReleaseGuard)
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.awake.backend.vulkan)
    // PointerCursor, the type of the cursor the window shows.
    implementation(libs.awake.engine.window)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

// Libraries ship their keep rules in META-INF/proguard, where Android's R8 finds them. Compose's
// ProGuard doesn't look there, so Release Guard collects them alongside curated desktop rules.
val collectLibraryKeepRules = tasks.named<CollectLibraryKeepRulesTask>("collectLibraryKeepRules")
val generateReleaseGuardRules = tasks.named<GenerateReleaseGuardRulesTask>("generateReleaseGuardRules")
val desktopProguardDir = layout.buildDirectory.dir("compose/tmp/main-release/proguard")

// Compose ProGuard outputs both the app's own JARs and every third-party library JAR into one
// folder. Stage the app's own obfuscated JARs separately so readability audits measure game code
// rather than kept JNI/AWT library classes while bytecode verification links the full classpath.
val stageDesktopOwnObfuscatedJars = tasks.register<Sync>("stageDesktopOwnObfuscatedJars") {
    dependsOn("proguardReleaseJars")
    from(desktopProguardDir) {
        include("desktopApp*.jar", "shared-jvm*.jar", "core-jvm*.jar")
    }
    into(layout.buildDirectory.dir("release-guard/desktop-own-jars"))
}

releaseGuard {
    projectPackages.add("com.awakekt.awake.template")
    minScore.set(80)
    failOnSeverity.set("HIGH")

    rules {
        mainClass.set("com.awakekt.awake.template.MainKt")
        desktopDontOptimize.set(true)
        stripKotlinMetadata.set(false)
    }

    desktop {
        enabled.set(true)
        collectLibraryKeepRules.set(true)
        gameJarsDir.set(layout.buildDirectory.dir("release-guard/desktop-own-jars"))
        librariesDir.set(desktopProguardDir)
        // The starter template has only ~14 obfuscated symbols total, so keeping MainKt alone is
        // ~0.071 readability; tighten this back to 0.02 as your game grows.
        maxReadability.set(0.10)
        verifyBytecode.set(true)
        forbidOriginalSourceFileAttribute.set(true)
        forbidDebugTablesAndKotlinMetadata.set(false)
        inspectNativeLibraries.set(false)
        forbidJpackageDebugFlags.set(true)
    }

    supplyChain {
        enabled.set(true)
        forbidSnapshotDependencies.set(true)
    }
}

compose.desktop {
    application {
        mainClass = "com.awakekt.awake.template.MainKt"
        // macOS only lets the first thread open windows; the Vulkan window opens from main().
        if (System.getProperty("os.name").startsWith("Mac")) jvmArgs += "-XstartOnFirstThread"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.awakekt.awake.template"
            packageVersion = "1.0.0"
        }

        // The release (`runRelease`, `packageRelease…`) is shrunk and obfuscated by ProGuard, with
        // the rules every library ships for it, Release Guard's curated rules, and your own in
        // proguard-rules.pro.
        buildTypes.release.proguard {
            obfuscate.set(true)
            // Compose's default, ProGuard 7.7.0, drops classes from a Kotlin sealed interface's
            // permitted subclasses, and the release then fails to load them. 7.10.0 keeps them all.
            version.set("7.10.0")
            configurationFiles.from(
                project.file("proguard-rules.pro"),
                generateReleaseGuardRules.flatMap { it.desktopRulesFile },
                collectLibraryKeepRules.map { it.outputDir.asFileTree },
            )
        }
    }
}

tasks.named("verifyReleaseGuard") {
    dependsOn(stageDesktopOwnObfuscatedJars)
}

// macOS has no system Vulkan: `run` points the loader at Homebrew's MoltenVK
// (`brew install molten-vk vulkan-loader`). Other systems find their driver without help.
tasks.withType<JavaExec>().configureEach {
    if (System.getProperty("os.name").startsWith("Mac")) {
        val icd = listOf("/opt/homebrew/Cellar/molten-vk", "/usr/local/Cellar/molten-vk")
            .flatMap { File(it).listFiles().orEmpty().toList() }
            .map { File(it, "etc/vulkan/icd.d/MoltenVK_icd.json") }
            .firstOrNull { it.isFile }
        icd?.let { environment("VK_ICD_FILENAMES", it.absolutePath) }
        environment("DYLD_FALLBACK_LIBRARY_PATH", "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib")
    }
}
