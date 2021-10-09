import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.5.31"
}

group = "com.jetbrains.exposed.gradle"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin", "kotlin-reflect",  "1.5.31")

    apply("deps.gradle.kts")
    val applyGeneratorDependencies = rootProject.extra["applyGeneratorDependencies"] as ((String, String, String) -> Unit) -> Unit

    applyGeneratorDependencies{ group, artifactId, version ->
        implementation(group, artifactId, version)
    }

    testImplementation("junit", "junit", "4.12")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("com.opentable.components", "otj-pg-embedded", "0.12.0")
    testImplementation("org.testcontainers", "testcontainers", "1.14.3")
    testImplementation("org.testcontainers", "mysql", "1.14.3")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType(JavaCompile::class) {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}

