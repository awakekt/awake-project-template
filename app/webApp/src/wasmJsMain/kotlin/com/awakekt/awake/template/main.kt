package com.awakekt.awake.template

import com.awakekt.awake.webgpu.application.WebGpuEngine
import com.awakekt.awake.webgpu.application.launchWebGpuGame
import com.awakekt.awake.engine.platform.dsl.AppWindowBackend

fun main() = launchWebGpuGame {
    WebGpuEngine(
        appLifecycle = createGame(AppWindowBackend.WEBGPU),
        requestedPlan = AwakeRenderPlan,
    )
}
