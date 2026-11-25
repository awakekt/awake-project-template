package com.awakekt.awake.template

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.awakekt.awake.core.io.AssetSource
import com.awakekt.awake.engine.window.AwakeSurfaceView
import com.awakekt.awake.vulkan.application.VulkanEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reading the project happens off the main thread; the view appears once it's loaded.
        lifecycleScope.launch {
            // A prebuilt APK exported by Awake Studio packs the project under assets/project/;
            // a Gradle-built app project keeps it on the classpath in BundledProject.
            val source = AssetSource { path ->
                runCatching {
                    withContext(Dispatchers.IO) {
                        assets.open("project/${path.value}").use { it.readBytes() }
                    }
                }.recoverCatching {
                    BundledProject.read(path).getOrThrow()
                }
            }
            val game = loadGame(source)
            val title = resolveWindowTitle(game)
            Log.i("AwakePlayer", "AWAKE_ANDROID_PLAYER_OK name=$title")
            val engine = VulkanEngine(createGame(game, touchControls = true, windowTitle = title), AwakeRenderPlan)
            setContentView(AwakeSurfaceView(this@MainActivity, engine))
        }
    }
}
