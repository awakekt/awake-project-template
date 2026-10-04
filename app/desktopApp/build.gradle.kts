import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.awake.backend.vulkan)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
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
    }
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
