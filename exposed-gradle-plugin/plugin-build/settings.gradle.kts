pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

rootProject.name = ("com.jetbrains.exposed.gradle.plugin")

include(":plugin")
includeBuild("exposed-code-generator")
