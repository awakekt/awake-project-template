rootProject.name = "AwakeProject"

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
        // An Awake Core release Maven Central refused under its publishing limits, served by Core at
        // Central's coordinates.
        maven("https://awakekt.github.io/awake/") { mavenContent { includeGroupAndSubgroups("com.awakekt.awake") } }
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
        // An Awake Core release Maven Central refused under its publishing limits, served by Core at
        // Central's coordinates.
        maven("https://awakekt.github.io/awake/") { mavenContent { includeGroupAndSubgroups("com.awakekt.awake") } }
        mavenLocal()
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
