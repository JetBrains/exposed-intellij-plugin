
plugins {
    id("com.jetbrains.exposed.gradle.plugin")
}
exposedCodeGeneratorConfig {
    connectionURL.set("jdbc:postgresql:pltest?user=postgres&password=testing")
}
