package org.jetbrains.exposed.gradle.types

import org.jetbrains.exposed.gradle.DatabaseFromScriptTest
import org.jetbrains.exposed.gradle.resourcesTestDataPath
import org.jetbrains.exposed.gradle.tests.TestDB
import org.junit.Test
import java.nio.file.Paths

class SQLiteTest : DatabaseFromScriptTest() {
    private fun runSQLiteTest(filename: String, tableName: String? = null) {
        testFromScriptAgainstKtFile(
                Paths.get(resourcesTestDataPath.toString(), "sqlite", "vartypes.sql"),
                Paths.get("sqlite", filename),
                tableName = tableName,
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE)
        )
    }

    @Test
    fun integerTypes() = runSQLiteTest("IntegerTypes.kt", "integer_types")

    @Test
    fun floatingPointTypes() = runSQLiteTest("FloatingPointTypes.kt", "floating_point_types")

    @Test
    fun decimalTypes() = runSQLiteTest("DecimalTypes.kt", "decimal_types")

    @Test
    fun longTypes() = runSQLiteTest("LongTypes.kt", "long_types")

    @Test
    fun charTypes() = runSQLiteTest("CharTypes.kt", "char_types")

    @Test
    fun datetimeTypes() = runSQLiteTest("DatetimeTypes.kt", "datetime_types")
}