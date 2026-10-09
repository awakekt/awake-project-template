import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // A game's own scene components are serializable, so its capabilities can define them here.
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// The bundled project's manifest names the game's own capabilities: the `plugins` entries with a
// `capabilityClass`. They are generated into `projectCapabilities`, which loadGame passes to loadProject,
// so the manifest stays the one list. An entry with an `artifact` (a published capability) adds that
// library too. A project without any generates an empty list.
// Read through `providers`, so the configuration cache notices an edited manifest.
@Suppress("UNCHECKED_CAST")
val projectPlugins: List<Map<String, Any?>> =
    providers.fileContents(layout.projectDirectory.file("src/commonMain/resources/project/awake.project.json")).asText.orNull
        ?.let { (groovy.json.JsonSlurper().parseText(it) as Map<String, Any?>)["plugins"] as? List<Map<String, Any?>> }
        .orEmpty()

val generateProjectCapabilities by tasks.registering {
    val capabilityClasses = projectPlugins.mapNotNull { it["capabilityClass"] as? String }
    val output = layout.buildDirectory.dir("generated/awake/capabilities")
    inputs.property("capabilityClasses", capabilityClasses)
    outputs.dir(output)
    doLast {
        val listed = if (capabilityClasses.isEmpty()) {
            "emptyList()"
        } else {
            capabilityClasses.joinToString(",\n", "listOf(\n", ",\n)") { "    $it" }
        }
        val file = output.get().file("com/awakekt/awake/template/ProjectCapabilities.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |// Generated from the bundled project's awake.project.json; edit the manifest, not this file.
            |package com.awakekt.awake.template
            |
            |import com.awakekt.awake.project.runtime.SceneCapability
            |
            |/** The game's own capabilities, as the bundled project's manifest names them. */
            |internal val projectCapabilities: List<SceneCapability> = $listed
            |""".trimMargin(),
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    android {
       namespace = "com.awakekt.awake.template.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        // Core 0.3 renamed the project runtime API. Keep the published 0.2 starter usable while
        // the engine's consumer gate builds this same template against the newer Core artifacts.
        val coreVersion = libs.versions.awake.asProvider().get().substringBefore('-').split('.').map(String::toInt)
        val legacyProjectRuntime = coreVersion[0] == 0 && coreVersion[1] < 3
        commonMain {
            kotlin.srcDir(if (legacyProjectRuntime) "src/core2Main/kotlin" else "src/core3Main/kotlin")
            // Core 0.2 has no capabilities to pass.
            if (!legacyProjectRuntime) kotlin.srcDir(generateProjectCapabilities)
        }
        // The Android library packages only its own Java resources, so it takes the project from commonMain's.
        androidMain {
            resources.srcDir("src/commonMain/resources")
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.awake.engine.bootstrap)
            implementation(libs.awake.asset.shaders)
            implementation(libs.awake.asset.shader.pack)
            implementation(libs.awake.core.host)
            implementation(libs.awake.scene.authoring)
            // api: loadGame and createGame hand LoadedProject to every app module.
            api(libs.awake.project.runtime)
            implementation(libs.awake.backend.jolt)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            projectPlugins.mapNotNull { it["artifact"] as? Map<*, *> }.forEach { artifact ->
                implementation("${artifact["group"]}:${artifact["name"]}:${artifact["version"]}")
            }
        }
        iosMain.dependencies {
            implementation(libs.awake.backend.vulkan)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
