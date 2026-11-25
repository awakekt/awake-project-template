package com.awakekt.awake.template

import com.awakekt.awake.core.io.AssetSource
import com.awakekt.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    val source = packedProjectSource() ?: BundledProject
    val data = runBlocking { loadGame(source) }
    val title = resolveWindowTitle(data, System.getProperty("awake.app.name"))
    if ("--smoke-test" in args || System.getProperty("awake.smokeTest") == "true") {
        println(runGameSmokeTest(data, title))
        return
    }
    val game = createGame(data, windowTitle = title)
    runVulkanDesktopGame(
        game,
        AwakeRenderPlan,
        cursor = pointerCursorOf(game),
        cursorImage = pointerCursorImageOf(game),
    )
}

/**
 * Reads a project packed into a prebuilt desktop app bundle (`-Dawake.project.dir=...`, e.g.
 * `Contents/Resources/project`), falling back to [BundledProject] when running from Gradle source.
 */
private fun packedProjectSource(): AssetSource? {
    val dir = System.getProperty("awake.project.dir")
        ?.takeIf(String::isNotBlank)
        ?.let(::File)
        ?.takeIf(File::isDirectory)
        ?: return null
    val root = dir.canonicalFile
    val prefix = root.path + File.separator
    return AssetSource { path ->
        runCatching {
            val target = File(root, path.value).canonicalFile
            require(target.path == root.path || target.path.startsWith(prefix)) {
                "Asset path escapes project directory: ${path.value}"
            }
            target.readBytes()
        }
    }
}
