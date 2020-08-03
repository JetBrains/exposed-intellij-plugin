package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.exposed.gradle.tests.TestDB
import java.nio.file.Path
import java.nio.file.Paths


val generalTestDataPath: Path = Paths.get("src", "test", "kotlin", "org", "jetbrains", "exposed", "gradle", "databases")
val resourcesDatabasesPath: Path = Paths.get("src", "test", "resources", "databases")
val resourcesConfigFilesPath: Path = Paths.get("src", "test", "resources", "config")

fun getDatabaseExposedFileSpec(db: TestDB, tableName: String? = null, configFileName: String? = null): List<FileSpec> {
    val metadataGetter = MetadataGetter(db.connection, db.user, db.pass)
    val tables = metadataGetter.getTables().filterUtilTables()
    val exposedCodeGenerator = if (tableName != null) {
        ExposedCodeGenerator(tables.filter { it.name.equals(tableName, ignoreCase = true) }, configFileName)
    } else {
        ExposedCodeGenerator(tables, configFileName)
    }
    return exposedCodeGenerator.generateExposedTables(db.name)
}

private fun List<schemacrawler.schema.Table>.filterUtilTables() = this.filterNot { it.fullName.startsWith("sys.") }

