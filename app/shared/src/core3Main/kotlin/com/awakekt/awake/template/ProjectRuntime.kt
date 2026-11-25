package com.awakekt.awake.template

import com.awakekt.awake.compose.ui.platform.InputOwnership
import com.awakekt.awake.core.input.Input
import com.awakekt.awake.core.io.AssetSource
import com.awakekt.awake.ecs.World
import com.awakekt.awake.physics.PhysicsWorld
import com.awakekt.awake.project.runtime.sceneSystems
import com.awakekt.awake.project.runtime.loadProject as loadCoreProject
import com.awakekt.awake.project.runtime.runProject as runCoreProject
import com.awakekt.awake.scene.authoring.SceneAppDsl
import com.awakekt.awake.scene.binding.instantiate
import com.awakekt.awake.scene.controls.GameplayInput

/** A loaded project on Core 0.3 and later. */
typealias LoadedProject = com.awakekt.awake.project.runtime.LoadedProject

/** Loads the project's entry scene using the selected physics backend. */
internal suspend fun loadProject(
    files: AssetSource,
    physicsWorld: suspend () -> PhysicsWorld,
): LoadedProject = loadCoreProject(files, physicsWorld = physicsWorld)

/** Runs the loaded project in this application's scene. */
internal fun SceneAppDsl.runProject(project: LoadedProject, touchControls: Boolean) {
    runCoreProject(project, touchControls)
}

/** The packed project's manifest name, used by prebuilt players when [GAME_TITLE] was not rewritten. */
internal fun projectTitleOf(project: LoadedProject): String? = project.manifest.name

/** Instantiates and ticks [project] headlessly for [frames] frames to verify a packed player export. */
internal fun smokeTestProject(project: LoadedProject, frames: Int): String {
    val world = World()
    project.scene.instantiate(world = world)
    val idle = GameplayInput(Input().currentSnapshot, InputOwnership())
    project.sceneSystems({ idle }).use { systems ->
        repeat(frames) {
            systems.fixed.forEach { it.update(world, SMOKE_STEP) }
            systems.frame.forEach { it.update(world, SMOKE_STEP) }
        }
    }
    return "project=${project.manifest.id} name=${project.manifest.name} scene=${project.manifest.entryScene} nodes=${project.scene.nodes.size} frames=$frames"
}

/** Releases resources held by [project] after a headless smoke test. */
internal fun closeProject(project: LoadedProject) {
    project.close()
}

private const val SMOKE_STEP = 1f / 60f
