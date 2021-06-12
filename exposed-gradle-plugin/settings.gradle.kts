pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "exposed-gradle"

include(":example")
includeBuild("plugin-build")
