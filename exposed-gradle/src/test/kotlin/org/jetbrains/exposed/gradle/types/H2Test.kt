package org.jetbrains.exposed.gradle.types

import org.jetbrains.exposed.gradle.DatabaseFromScriptTest
import org.jetbrains.exposed.gradle.resourcesTestDataPath
import org.jetbrains.exposed.gradle.tests.TestDB
import org.junit.Test
import java.nio.file.Paths

class H2Test : DatabaseFromScriptTest() {
    private fun runH2Test(filename: String, tableName: String? = null) {
        testFromScriptAgainstKtFile(
                Paths.get(resourcesTestDataPath.toString(), "h2", "vartypes.sql"),
                Paths.get("h2", filename),
                tableName = tableName,
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.H2)
        )
    }

    @Test
    fun integerTypes() = runH2Test("IntegerTypes.kt", "integer_types")

    @Test
    fun booleanTypes() = runH2Test("BooleanTypes.kt", "boolean_types")

    @Test
    fun smallIntTypes() = runH2Test("SmallIntTypes.kt", "small_int_types")

    @Test
    fun longTypes() = runH2Test("LongTypes.kt", "long_types")

    @Test
    fun decimalTypes() = runH2Test("DecimalTypes.kt", "decimal_types")

    @Test
    fun doubleTypes() = runH2Test("DoubleTypes.kt", "double_types")

    @Test
    fun floatTypes() = runH2Test("FloatTypes.kt", "float_types")

    @Test
    fun charTypes() = runH2Test("CharTypes.kt", "char_types")

    @Test
    fun varcharTypes() = runH2Test("VarcharTypes.kt", "varchar_types")

    @Test
    fun textTypes() = runH2Test("TextTypes.kt", "text_types")

    @Test
    fun miscTypes() = runH2Test("MiscTypes.kt", "misc_types")

    @Test
    fun binaryTypes() = runH2Test("BinaryTypes.kt", "binary_types")

    @Test
    fun datetimeTypes() = runH2Test("DatetimeTypes.kt", "datetime_types")
}