// The root project name has to match the addon namespace (see addonInfo in build.gradle.kts),
// otherwise the mixins are not applied outside of the development environment.
rootProject.name = "xqedii"

pluginManagement {
    repositories {
        maven("https://maven.laby.net/api/v1/maven/release/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("net.labymod.labygradle.settings") version "0.9.0"
    }
}

plugins {
    id("net.labymod.labygradle.settings")
}

include(":api")
include(":core")
