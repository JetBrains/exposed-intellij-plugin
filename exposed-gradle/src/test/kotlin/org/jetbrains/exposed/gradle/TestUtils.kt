package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.FileSpec
import org.jetbrains.exposed.gradle.tests.TestDB
import org.junit.Assert
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


val generalTestDataPath: Path = Paths.get("src", "test", "kotlin", "org", "jetbrains", "exposed", "gradle", "databases")
val resourcesTestDataPath: Path = Paths.get("src", "test", "resources", "databases")

fun getDatabaseExposedFileSpec(db: TestDB, tableName: String? = null): FileSpec {
    val metadataGetter = MetadataGetter(db.connection, db.user, db.pass)
    val tables = metadataGetter.getTables().filterUtilTables()
    val exposedCodeGenerator = if (tableName != null) {
        ExposedCodeGenerator(tables.filter { it.name.equals(tableName, ignoreCase = true) })
    } else {
        ExposedCodeGenerator(tables)
    }
    return exposedCodeGenerator.generateExposedTables(db.name)
}

fun checkDatabaseMetadataAgainstFile(
        db: TestDB,
        testDataDirectoryPath: Path,
        testDataFilepath: Path,
        tableName: String? = null,
        fileParentPath: String = "" // TODO path but string? bad
) {
    val fileSpec = getDatabaseExposedFileSpec(db, tableName)
    val sb = StringBuilder()
    fileSpec.writeTo(sb)
    val fileLines = sb.splitToSequence("\n").toList()
    val lines = fileLines.filterKtFileLines().map { it.trim() }

    val p = Paths.get(testDataDirectoryPath.toString(), fileParentPath)
    // this should take care of separators right
    val expectedFileLines = File(p.toFile(), testDataFilepath.toString()).readLines()
    val expectedLines = expectedFileLines.filterKtFileLines().map { it.trim() }

    val imports = fileLines.filterImportsOnly()
    val expectedImports = expectedFileLines.filterImportsOnly()
    expectedImports.forEach { Assert.assertTrue(it in imports) }

    Assert.assertTrue(lines.size == expectedLines.size)
    lines.forEach { Assert.assertTrue(it in expectedLines) }
}

private fun List<schemacrawler.schema.Table>.filterUtilTables() = this.filterNot { it.fullName.startsWith("sys.") }

private fun List<String>.filterKtFileLines() = this.filterNot {
    it.isBlank() || it.startsWith("import ") || it.startsWith("package ")
}

private fun List<String>.filterImportsOnly() = filter { it.startsWith("import ") }

