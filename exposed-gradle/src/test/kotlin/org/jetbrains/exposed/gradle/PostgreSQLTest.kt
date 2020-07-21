package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.tests.TestDB
import org.junit.Test
import java.nio.file.Paths

class PostgreSQLTest : DatabaseTypesTest() {
    private fun runPostgreSQLTest(filename: String, tableName: String? = null) {
        testFromScriptAgainstKtFile(
                Paths.get(resourcesTestDataPath.toString(), "vartypes_psql", "vartypes.sql"),
                Paths.get("vartypes_psql", filename),
                tableName = tableName,
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun integerTypes() = runPostgreSQLTest("IntegerTypes.kt", "integer_types")

    @Test
    fun floatingPointTypes() = runPostgreSQLTest("FloatingPointTypes.kt", "floating_point_types")

    @Test
    fun decimalTypes() = runPostgreSQLTest("DecimalTypes.kt", "decimal_types")

    @Test
    fun longTypes() = runPostgreSQLTest("LongTypes.kt", "long_types")

    @Test
    fun charTypes() = runPostgreSQLTest("CharTypes.kt", "char_types")

    @Test
    fun smallIntTypes() = runPostgreSQLTest("SmallIntTypes.kt", "small_int_types")

    @Test
    fun miscTypes() = runPostgreSQLTest("MiscTypes.kt", "misc_types")

}