import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    java
    kotlin("jvm") version "1.5.10"
}

group = "com.jetbrains.exposed.gradle"
version = "1.0"

repositories {
    mavenCentral()
}

object Versions {
    const val KOTLIN = "1.5.10"
    const val EXPOSED = "0.32.1"
    const val SCHEMA_CRAWLER = "16.9.3"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin", "kotlin-reflect", Versions.KOTLIN)

    // exposed
    implementation("org.jetbrains.exposed", "exposed-core", Versions.EXPOSED)
    implementation("org.jetbrains.exposed", "exposed-dao", Versions.EXPOSED)
    implementation("org.jetbrains.exposed", "exposed-jdbc", Versions.EXPOSED)
    implementation("org.jetbrains.exposed", "exposed-java-time", Versions.EXPOSED)

    // schemacrawler
    implementation("us.fatehi", "schemacrawler", Versions.SCHEMA_CRAWLER)
    implementation("us.fatehi", "schemacrawler-mysql", Versions.SCHEMA_CRAWLER)
    implementation("us.fatehi", "schemacrawler-sqlite", Versions.SCHEMA_CRAWLER)
    implementation("us.fatehi", "schemacrawler-postgresql", Versions.SCHEMA_CRAWLER)

    // utils
    implementation("org.apache.commons", "commons-text", "1.8")
    // TODO replace by anything else that can perform topsort without me having to write it from scratch
//    implementation("com.github.Erdos-Graph-Framework:Erdos:v1.0")
    implementation("com.facebook.presto", "presto-parser", "0.239")


    // kotlin code generation/testing
    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.2")

    // yaml config files
    implementation("com.sksamuel.hoplite", "hoplite-yaml", "1.3.3")

    // logging
    implementation("org.slf4j:slf4j-api:1.7.30")
//    implementation( "ch.qos.logback", "logback-classic", "1.2.3")

    // database drivers
    implementation("com.h2database", "h2","1.4.199")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("org.xerial:sqlite-jdbc:3.32.3")
    implementation("org.mariadb.jdbc", "mariadb-java-client", "2.6.0")
    implementation("mysql", "mysql-connector-java", "8.0.25")
    implementation("com.impossibl.pgjdbc-ng", "pgjdbc-ng", "0.8.4")
    implementation("com.oracle.database.jdbc", "ojdbc8", "12.2.0.1")
    implementation("com.microsoft.sqlserver", "mssql-jdbc", "8.2.2.jre8")

    testImplementation("junit", "junit", "4.12")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("com.opentable.components", "otj-pg-embedded", "0.12.0")
    testImplementation("org.testcontainers", "testcontainers", "1.14.3")
    testImplementation("org.testcontainers", "mysql", "1.14.3")
}

configure<JavaPluginConvention> {
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

