package com.awakekt.awake.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.awakekt.awake.engine.platform.VulkanView
import com.awakekt.awake.vulkan.application.VulkanEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val engine = VulkanEngine(createGame(), AwakeRenderPlan)
        setContentView(VulkanView(this, engine))
    }
}
