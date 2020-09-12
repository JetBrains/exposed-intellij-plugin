object PluginCoordinates {
    const val ID = "com.jetbrains.exposed.gradle.plugin"
    const val GROUP = "com.jetbrains.exposed.gradle"
    const val VERSION = "0.0.1"
    const val IMPLEMENTATION_CLASS = "com.jetbrains.exposed.gradle.plugin.ExposedGradlePlugin"
}

object PluginBundle {
    const val VCS = "https://github.com/JetBrains/exposed-intellij-plugin"
    const val WEBSITE = "https://github.com/JetBrains/exposed-intellij-plugin"
    const val DESCRIPTION = "Exposed ORM framework plugin"
    const val DISPLAY_NAME = "Exposed ORM framework plugin"
    val TAGS = listOf(
        "plugin",
        "kotlin",
        "exposed",
        "database"
    )
}

