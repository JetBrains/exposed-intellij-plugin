import org.gradle.kotlin.dsl.extra

rootProject.extra["applyGeneratorDependencies"] = { implementation: (group: String, artifactId: String, version: String) -> Unit ->
    applyGeneratorDependencies(implementation)
}

fun applyGeneratorDependencies(implementation: (group: String, artifactId: String, version: String) -> Unit) {
    val exposedVersion = "0.35.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    val schemaCrawlerVersion = "16.15.7"
    implementation("us.fatehi", "schemacrawler", schemaCrawlerVersion)
    implementation("us.fatehi", "schemacrawler-mysql", schemaCrawlerVersion)
    implementation("us.fatehi", "schemacrawler-sqlite", schemaCrawlerVersion)
    implementation("us.fatehi", "schemacrawler-postgresql", schemaCrawlerVersion)

    // utils
    implementation("org.apache.commons", "commons-text", "1.10.0")
    // TODO replace by anything else that can perform topsort without me having to write it from scratch
//    implementation("com.github.Erdos-Graph-Framework:Erdos:v1.0")
    implementation("com.facebook.presto", "presto-parser", "0.239")


    // kotlin code generation/testing
    implementation("com.squareup", "kotlinpoet", "1.10.1")
    implementation("com.github.tschuchortdev", "kotlin-compile-testing", "1.4.2")

    // yaml config files
    implementation("com.sksamuel.hoplite", "hoplite-yaml", "1.4.9")

    // logging
    implementation("org.slf4j", "slf4j-api", "1.7.30")

    // database drivers
    implementation("com.h2database", "h2", "1.4.199")
    implementation("org.postgresql", "postgresql", "42.2.2")
    implementation("org.xerial", "sqlite-jdbc", "3.32.3")
    implementation("org.mariadb.jdbc", "mariadb-java-client", "2.6.0")
    implementation("mysql", "mysql-connector-java", "8.0.25")
    implementation("com.impossibl.pgjdbc-ng", "pgjdbc-ng", "0.8.4")
    implementation("com.oracle.database.jdbc", "ojdbc8", "12.2.0.1")
    implementation("com.microsoft.sqlserver", "mssql-jdbc", "8.2.2.jre8")
}