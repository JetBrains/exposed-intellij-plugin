pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = ("com.jetbrains.exposed.gradle.plugin")

include(":plugin")
includeBuild("exposed-code-generator")
