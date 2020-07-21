package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.databases.*
import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

// run tests from a .kt file, check against the same file
class ExposedCodeGeneratorFromExposedTest : DatabaseTestsBase() {
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
            checkDatabaseMetadataAgainstFile(it, generalTestDataPath, testDataFilepath, tableName)
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
    fun decimalTypes() {
        testOnFile(Paths.get("DecimalTypes.kt"), listOf(DecimalTypes), "decimal_types")
    }

    // why does exposed map a float column to double?
/*    @Test
    fun floatingPointTypes() {
        testOnFile(Paths.get("FloatingPointTypes.kt"), listOf(FloatingPointTypes), excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE))
    }*/

    @Test
    fun charTypes() {
        testOnFile(Paths.get("CharTypes.kt"), listOf(CharTypes), "char_types")
    }

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

    // Exposed UUID gets mapped to binary
    // and I don't know what to do about that yet
    /*@Test
    fun uuidTableMySQL() {
        testOnFile(
                Paths.get("mysql", "IdTables.kt"),
                listOf(Sample1, Sample2, Sample4),
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.MYSQL)
        )
    }*/
}

// TODO refactor
// run tests from a sql script, check against a kt file
abstract class DatabaseTypesTest : DatabaseTestsBase() {
    protected fun testFromScriptAgainstKtFile(
            scriptFilepath: Path,
            testDataFilepath: Path,
            tableName: String? = null,
            excludedDbList: List<TestDB> = emptyList()
    ) {
        withDb(excludeSettings = excludedDbList, statement = {
            val script = scriptFilepath.toFile().readText()
            val splitResults = script.split(Regex("((?<=INSERT)|(?=INSERT))|((?<=CREATE)|(?=CREATE))|((?<=DROP)|(?=DROP))|((?<=--)|(?=--))")).filterNot { it.isBlank() }
            val commands = mutableListOf<String>()
            for (i in splitResults.indices step 2) {
                commands.add("${splitResults[i]} ${splitResults[i + 1]}")
            }
            commands.forEach { exec(it) }
            commit()
            checkDatabaseMetadataAgainstFile(it, resourcesTestDataPath, testDataFilepath, tableName)
        })
    }
}