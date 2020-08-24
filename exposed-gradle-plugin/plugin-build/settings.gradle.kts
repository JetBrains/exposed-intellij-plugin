pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = ("com.jetbrains.exposed.gradle.plugin")

include(":plugin")
includeBuild("exposed-code-generator")
