plugins {
    id("com.jetbrains.exposed.gradle.plugin")
}

exposedCodeGeneratorConfig {
    connectionURL = "jdbc:sqlite:${project.file("chinook.db").absolutePath}"
    user = "test"
    password = "test"
}
