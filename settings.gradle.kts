rootProject.name = "AwakeTemplate"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()
    }
}

// Optional local development substitution. A published Awake release remains the default for
// anyone cloning this template, while a sibling Awake checkout can validate the template before
// the next engine snapshot is available from Maven.
if (file("../awaken").isDirectory) {
    includeBuild("../awaken") {
        dependencySubstitution {
            substitute(module("com.awakekt.awake.engine:bootstrap"))
                .using(project(":awake:engine:bootstrap"))
            substitute(module("com.awakekt.awake.asset:shaders"))
                .using(project(":awake:asset:shaders"))
            substitute(module("com.awakekt.awake.asset:shader-pack"))
                .using(project(":awake:asset:shader-pack"))
            substitute(module("com.awakekt.awake.core:host"))
                .using(project(":awake:core:host"))
            substitute(module("com.awakekt.awake.engine:window"))
                .using(project(":awake:engine:window"))
            substitute(module("com.awakekt.awake.scene:authoring"))
                .using(project(":awake:scene:authoring"))
            substitute(module("com.awakekt.awake.project:runtime"))
                .using(project(":awake:project:runtime"))
            substitute(module("com.awakekt.awake.backend:jolt"))
                .using(project(":awake:backend:jolt"))
            substitute(module("com.awakekt.awake.backend:vulkan"))
                .using(project(":awake:backend:vulkan"))
            substitute(module("com.awakekt.awake.backend:webgpu"))
                .using(project(":awake:backend:webgpu"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":app:androidApp")
include(":app:desktopApp")
include(":app:shared")
include(":app:webApp")
include(":core")
include(":server")
