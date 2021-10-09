import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    api("com.jetbrains.exposed.gradle:exposed-code-generator")
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())

    implementation("com.squareup:kotlinpoet:1.10.1")
    implementation("us.fatehi:schemacrawler:16.15.7")

    compileOnly("com.github.johnrengelman.shadow:com.github.johnrengelman.shadow.gradle.plugin:7.0.0")
    api("org.ow2.asm", "asm", "9.2")
    api("org.ow2.asm", "asm-util", "9.2")

    testImplementation(TestingLib.JUNIT)

    apply("../exposed-code-generator/deps.gradle.kts")
    val applyGeneratorDependencies = rootProject.extra["applyGeneratorDependencies"] as ((String, String, String) -> Unit) -> Unit

    applyGeneratorDependencies{ group, artifactId, version ->
        implementation(group, artifactId, version)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create(PluginCoordinates.ID) {
            id = PluginCoordinates.ID
            implementationClass = PluginCoordinates.IMPLEMENTATION_CLASS
            version = PluginCoordinates.VERSION
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = PluginBundle.WEBSITE
    vcsUrl = PluginBundle.VCS
    description = PluginBundle.DESCRIPTION
    tags = PluginBundle.TAGS

    plugins {
        getByName(PluginCoordinates.ID) {
            displayName = PluginBundle.DISPLAY_NAME
        }
    }

    withDependencies {
        removeIf { it.artifactId == "exposed-code-generator" }
    }
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}

tasks.jar {
    val exposedCodeGenerator = gradle.includedBuild("exposed-code-generator")
    val exposedCodeGeneratorOutput =
        files(exposedCodeGenerator.projectDir.absolutePath + "/build/classes/kotlin/main/")
    from(exposedCodeGeneratorOutput)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

