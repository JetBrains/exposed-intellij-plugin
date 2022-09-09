plugins {
    id("com.jetbrains.exposed.gradle.plugin")
    kotlin("jvm")
}

dependencies {
    implementation("org.jetbrains.exposed", "exposed-core", "0.39.2")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.39.2")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.39.2")
    implementation("org.jetbrains.exposed", "exposed-java-time", "0.39.2")
}

exposedCodeGeneratorConfig {
    connectionURL = "jdbc:sqlite:${project.file("chinook.db").absolutePath}"
    user = "test"
    password = "test"
    outputDirectory.set(file("src/main/kotlin"))
}
