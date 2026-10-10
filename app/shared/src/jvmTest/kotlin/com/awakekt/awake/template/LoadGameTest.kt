package com.awakekt.awake.template

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bundled project loads as the app plays it, with every capability its manifest names: Core
 * refuses a project whose required capability the app doesn't link.
 */
class LoadGameTest {
    @Test
    fun theBundledProjectLoadsWithTheCapabilitiesItsManifestNames() {
        val project = runBlocking { loadGame() }
        project.use {
            assertEquals(projectCapabilities.size, it.manifest.plugins.count { plugin -> plugin.capabilityClass != null })
        }
    }
}
