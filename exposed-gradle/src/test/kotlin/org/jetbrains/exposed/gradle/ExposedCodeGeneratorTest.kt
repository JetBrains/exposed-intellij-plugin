package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.databases.*
import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


class ExposedCodeGeneratorTest {
    private fun checkDatabaseMetadataAgainstFile(
            databaseName: String,
            databaseDriver: String,
            testDataFilename: String,
            tableName: String? = null,
            fileParentPath: String = "",
            databaseMode: String? = null
    ) {
        val dbMode = if (databaseMode != null) ";MODE=$databaseMode" else ""
        val name = if (fileParentPath.isNotBlank()) {
            "./src/test/resources/databases/$fileParentPath/$databaseName$dbMode"
        } else {
            "./src/test/resources/databases/$databaseName$dbMode"
        }
        val metadataGetter = MetadataGetter(databaseDriver, name)
        val tables = metadataGetter.getTables()
        val exposedCodeGenerator = if (tableName != null) {
            ExposedCodeGenerator(tables.filter { it.name.equals(tableName, ignoreCase = true) })
        } else {
            ExposedCodeGenerator(tables)
        }
        val fileSpec = exposedCodeGenerator.generateExposedTables(databaseName)
        val sb = StringBuilder()
        fileSpec.writeTo(sb)
        // TODO potentially check imports and/or packages
        val lines = sb.splitToSequence("\n").filterNot { it.startsWith("import ") || it.startsWith("package ") || it.isBlank() }.toList().map { it.trim() }

        val p = Paths.get("src", "test", "resources", "databases", fileParentPath)
        val expectedLines = File(p.toFile(), testDataFilename).readLines()
                .filterKtFileLines()
                .map { it.trim() }
        assertTrue(lines.size == expectedLines.size)
        lines.forEach { assertTrue(it in expectedLines) }
    }

    private fun List<String>.filterKtFileLines(): List<String> = this.filterNot {
        it.isBlank() || it.startsWith("import ") || it.startsWith("package ")
    }

    @Test
    fun oneTableTest() {
        checkDatabaseMetadataAgainstFile("example.db", "sqlite", "example.kt")
    }

    private fun sqliteTypesTest(tableName: String, exposedTableFilename: String) {
        checkDatabaseMetadataAgainstFile("vartypes.db", "sqlite", exposedTableFilename, tableName, "vartypes_sqlite")
    }

    @Test
    fun sqliteIntegerTypesTest() {
        sqliteTypesTest("integer_types", "IntegerTypes.kt")
    }

    @Test
    // sqlite types: real -> float; float, double -> double
    fun sqliteFloatingPointTypesTest() {
        sqliteTypesTest("floating_point_types", "FloatingPointTypes.kt")
    }

    @Test
    // important to note that's it's sqlite and it probably provides 2*10^9 and 10 as default values
    fun sqliteNumericTypesTest() {
        sqliteTypesTest("numeric_types", "NumericTypes.kt")
    }

    @Test
    fun sqliteLongTypesTest() {
        sqliteTypesTest("long_types", "LongTypes.kt")
    }

    @Test
    fun sqliteCharTypesTest() {
        sqliteTypesTest("char_types", "CharTypes.kt")
    }

    @Test
    fun intIdTableTest() {
        checkDatabaseMetadataAgainstFile("idpk.db", "sqlite", "IdPk.kt")
    }

    // TODO restore?
    /*@Test
    fun textIdTableTest() {
        checkDatabaseMetadataAgainstFile("textpk.db", "sqlite", "TextPk.kt")
    }*/

    private fun h2TypesSetUp(h2FilePath: Path) {
        val dbFile = Paths.get("src", "test", "resources", "databases", h2FilePath.toString()).toFile()
        dbFile.copyTo(Paths.get(dbFile.parent, "copy.db.mv.db").toFile(), overwrite = true)
    }

    private fun h2TypesTearDown(h2FilePath: Path) {
        val dbFile = Paths.get("src", "test", "resources", "databases", h2FilePath.toString()).toFile()
        val copyFile = Paths.get(dbFile.parent, "copy.db.mv.db").toFile()
        copyFile.copyTo(dbFile, overwrite = true)
        copyFile.delete()
    }

    private fun h2TypesTest(tableName: String, exposedTableFilename: String) {
        val path = Paths.get("vartypes_h2", "h2vartypes.db.mv.db")
        h2TypesSetUp(path)
        checkDatabaseMetadataAgainstFile("h2vartypes.db", "h2:file", exposedTableFilename, tableName, "vartypes_h2")
        h2TypesTearDown(path)
    }

    @Test
    fun h2IntegerTypesTest() {
        h2TypesTest("integer_types", "IntegerTypes.kt")
    }

    @Test
    fun h2BooleanTypesTest() {
        h2TypesTest("boolean_types", "BooleanTypes.kt")
    }

    @Test
    fun h2SmallIntTypesTest() {
        h2TypesTest("small_int_types", "SmallIntTypes.kt")
    }

    @Test
    // h2 identity maps to java Long but gives an IdTable
    fun h2LongTypesTest() {
        h2TypesTest("long_types", "LongTypes.kt")
    }

    @Test
    fun h2DecimalTypesTest() {
        h2TypesTest("decimal_types", "DecimalTypes.kt")
    }

    @Test
    fun h2DoubleTypesTest() {
        h2TypesTest("double_types", "DoubleTypes.kt")
    }

    @Test
    fun h2FloatTypesTest() {
        h2TypesTest("float_types", "FloatTypes.kt")
    }

    @Test
    fun h2CharTypesTest() {
        h2TypesTest("char_types", "CharTypes.kt")
    }

    @Test
    fun h2VarcharTypesTest() {
        h2TypesTest("varchar_types", "VarcharTypes.kt")
    }

    @Test
    fun h2TextTypesTest() {
        h2TypesTest("text_types", "TextTypes.kt")
    }

    @Test
    fun h2MiscTypesTest() {
        h2TypesTest("misc_types", "MiscTypes.kt")
    }

    @Test
    fun h2BinaryTypesTest() {
        h2TypesTest("binary_types", "BinaryTypes.kt")
    }

    private fun psqlTypesTest(tableName: String, exposedTableFilename: String) {
        val path = Paths.get("vartypes_psql", "h2_psql_vartypes.db.mv.db")
        h2TypesSetUp(path)
        checkDatabaseMetadataAgainstFile(
                "h2_psql_vartypes.db",
                "h2:file",
                exposedTableFilename,
                tableName,
                "vartypes_psql",
                "PostgreSQL"
        )
        h2TypesTearDown(path)
    }

    @Test
    fun psqlIntegerTypesTest() {
        psqlTypesTest("integer_types", "IntegerTypes.kt")
    }

    @Test
    fun psqlFloatingPointTypesTest() {
        psqlTypesTest("floating_point_types", "FloatingPointTypes.kt")
    }

    @Test
    fun psqlLongTypesTest() {
        psqlTypesTest("long_types", "LongTypes.kt")
    }

    @Test
    // be wary of precision and scale values when they are not explicitly stated by the user
    // h2 psql gives 65535/32767; actual postgres may use different values
    fun psqlNumericTypesTest() {
        psqlTypesTest("numeric_types", "NumericTypes.kt")
    }

    @Test
    fun psqlCharTypesTest() {
        psqlTypesTest("char_types", "CharTypes.kt")
    }

    @Test
    fun psqlSmallIntTypesTest() {
        psqlTypesTest("small_int_types", "SmallIntTypes.kt")
    }

    @Test
    fun psqlMiscTypesTest() {
        psqlTypesTest("misc_types", "MiscTypes.kt")
    }

    @Test
    fun h2ColumnReferenceTest() {
        checkDatabaseMetadataAgainstFile("h2ref.db", "h2:file", "RefTable.kt")
    }
}

class ExposedCodeGeneratorFromExposedTest : DatabaseTestsBase() {
    private fun checkDatabaseMetadataAgainstFile(
            db: TestDB,
            testDataFilepath: Path,
            tableName: String? = null,
            fileParentPath: String = ""
    ) {
        val metadataGetter = MetadataGetter(db.connection, db.user, db.pass)
        val tables = metadataGetter.getTables().filterUtilTables()
        val exposedCodeGenerator = if (tableName != null) {
            ExposedCodeGenerator(tables.filter { it.name.equals(tableName, ignoreCase = true) })
        } else {
            ExposedCodeGenerator(tables)
        }
        val fileSpec = exposedCodeGenerator.generateExposedTables(db.name)
        val sb = StringBuilder()
        fileSpec.writeTo(sb)
        val fileLines = sb.splitToSequence("\n").toList()
        val lines = fileLines.filterKtFileLines().map { it.trim() }

        val p = Paths.get("src", "test", "kotlin", "org", "jetbrains", "exposed", "gradle", "databases", fileParentPath)
        // this should take care of separators right
        val expectedFileLines = File(p.toFile(), testDataFilepath.toString()).readLines()
        val expectedLines = expectedFileLines.filterKtFileLines().map { it.trim() }

        val imports = fileLines.filterImportsOnly()
        val expectedImports = expectedFileLines.filterImportsOnly()
        expectedImports.forEach { assertTrue(it in imports) }

        assertTrue(lines.size == expectedLines.size)
        lines.forEach { assertTrue(it in expectedLines) }
    }

    private fun List<schemacrawler.schema.Table>.filterUtilTables() = this.filterNot { it.fullName.startsWith("sys.") }

    private fun List<String>.filterKtFileLines() = this.filterNot {
        it.isBlank() || it.startsWith("import ") || it.startsWith("package ")
    }

    private fun List<String>.filterImportsOnly() = filter { it.startsWith("import ") }

    private fun testOnFile(
            testDataFilepath: Path,
            tables: List<Table>,
            tableName: String? = null,
            excludedDbList: List<TestDB> = emptyList()
    ) {
        withDb(excludeSettings = excludedDbList, statement = {
            for (table in tables) {
                SchemaUtils.drop(table)
                SchemaUtils.create(table)
            }
            checkDatabaseMetadataAgainstFile(it, testDataFilepath, tableName)
            for (table in tables) {
                SchemaUtils.drop(table)
            }
        })
    }

    @Test
    fun integerTypes() {
        testOnFile(
                Paths.get("IntegerTypes.kt"),
                listOf(IntegerTypes),
                "integer_types",
                excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    // no 'tinyint' in postgres, only 2, 4, or 8 bytes
    fun integerTypesPostgres() {
        testOnFile(
                Paths.get("postgresql", "IntegerTypes.kt"),
                listOf(IntegerTypes),
                "integer_types",
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun numericTypes() {
        testOnFile(Paths.get("NumericTypes.kt"), listOf(NumericTypes), "numeric_types")
    }

    // why does exposed map a float column to double?
    /*@Test
    fun floatingPointTypes() {
        testOnFile("FloatingPointTypes.kt", listOf(FloatingPointTypes))
    }*/

    @Test
    fun charTypes() {
        testOnFile(Paths.get("CharTypes.kt"), listOf(CharTypes), "char_types")
    }

    // The length of the Binary column is missing.
    @Test
    fun miscTypes() {
        testOnFile(
                Paths.get("MiscTypes.kt"),
                listOf(MiscTypes),
                "misc_types",
                excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    // can't specify the length in postgres
    fun miscTypesPostgres() {
        testOnFile(
                Paths.get("postgresql", "MiscTypes.kt"),
                listOf(MiscTypes),
                "misc_types",
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun idTables() {
        testOnFile(
                Paths.get("IdTables.kt"),
                listOf(Sample1, Sample2, Sample3, Sample4),
                excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL)
        )
    }

    @Test
    // Exposed long gets mapped to Integer and there's no way to retrieve Long back
    // Exposed UUID gets mapped to binary
    fun longIdTableSQLite() {
        testOnFile(
                Paths.get("sqlite", "IdTables.kt"),
                listOf(Sample1, Sample2, Sample4),
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE)
        )
    }

    @Test
    // Exposed UUID gets mapped to binary
    fun uuidTableMySQL() {
        testOnFile(
                Paths.get("mysql", "IdTables.kt"),
                listOf(Sample1, Sample2, Sample4),
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.MYSQL)
        )
    }
}