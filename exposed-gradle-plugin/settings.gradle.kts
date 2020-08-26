pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
}

rootProject.name = ("exposed-gradle")

include(":example")
includeBuild("plugin-build")
