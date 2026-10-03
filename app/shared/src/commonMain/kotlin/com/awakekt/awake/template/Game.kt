package com.awakekt.awake.template

import com.awakekt.awake.asset.shaders.RenderPlan
import com.awakekt.awake.asset.shaders.ScenePipeline
import com.awakekt.awake.asset.shaders.shaderSet
import com.awakekt.awake.core.geometry.VertexFormat
import com.awakekt.awake.engine.bootstrap.dsl.app
import com.awakekt.awake.engine.platform.dsl.AppWindowBackend
import com.awakekt.awake.engine.bootstrap.dsl.select
import com.awakekt.awake.engine.platform.lifecycle.AwakeAppLifecycle
import com.awakekt.awake.render.pipeline.PipelineKey

/** Creates a fresh application lifecycle for a platform host. */
fun createGame(windowBackend: AppWindowBackend = AppWindowBackend.VULKAN): AwakeAppLifecycle = app {
    window {
        title = "Awake Template"
        size(1280, 720)
        backend.select(windowBackend)
    }

    render { frame ->
        // Add simulation and gameplay updates here.
        frame.delta
    }
}

/** Shared pipeline declaration consumed by Vulkan and WebGPU. */
val AwakeRenderPlan = RenderPlan(
    primary = ScenePipeline(
        key = PipelineKey.Primary,
        shaders = shaderSet("triangle", emptyMap()),
        vertexFormat = VertexFormat.PositionColor,
    ),
)
