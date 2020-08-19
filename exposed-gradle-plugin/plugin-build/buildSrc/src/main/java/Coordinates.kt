object PluginCoordinates {
    const val ID = "com.jetbrains.exposed.gradle.plugin"
    const val GROUP = "com.jetbrains.exposed.gradle"
    const val VERSION = "1.0.0"
    const val IMPLEMENTATION_CLASS = "com.jetbrains.exposed.gradle.plugin.ExposedGradlePlugin"
}

object PluginBundle {
    const val VCS = "https://github.com/cortinico/kotlin-gradle-plugin-template"
    const val WEBSITE = "https://github.com/cortinico/kotlin-gradle-plugin-template"
    const val DESCRIPTION = "An empty Gradle plugin created from a template"
    const val DISPLAY_NAME = "An empty Gradle Plugin from a template"
    val TAGS = listOf(
        "plugin",
        "gradle",
        "sample",
        "template"
    )
}

