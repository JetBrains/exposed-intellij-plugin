package org.jetbrains.exposed

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.lang.StringBuilder
import java.nio.file.Paths

class MetadataGetterTest {
    private fun checkDatabaseMetadataAgainstFile(
            databaseName: String,
            databaseDriver: String,
            testDataFilename: String,
            tableName: String? = null,
            vararg fileParentPath: String
    ) {
        val fileSpec = generateExposedTablesForDatabase(databaseDriver, "./src/test/resources/databases/$databaseName", null, null, tableName)
        val sb = StringBuilder()
        fileSpec.writeTo(sb)
        val lines = sb.splitToSequence("\n").filterNot { it.startsWith("import ") || it.isBlank() }.toList().map { it.trim() }

        val p = Paths.get("src", "test", "resources", "databases", *fileParentPath)
        val expectedLines = File(p.toFile(), testDataFilename).readLines().filterNot { it.isBlank() }.map { it.trim() }
        assertTrue(lines.size == expectedLines.size)
        lines.forEach { assertTrue(it in expectedLines) }
    }

    @Test
    fun oneTableTest() {
        checkDatabaseMetadataAgainstFile("example.db", "sqlite", "example.kt")
    }

    private fun sqliteTypesTest(tableName: String, exposedTableFilename: String) {
        checkDatabaseMetadataAgainstFile("vartypes_sqlite/vartypes.db", "sqlite", exposedTableFilename, tableName, "vartypes_sqlite")
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

    @Test
    fun textIdTableTest() {
        checkDatabaseMetadataAgainstFile("textpk.db", "sqlite", "TextPk.kt")
    }

    // TODO generalize
    private fun h2TypesTest(tableName: String, exposedTableFilename: String) {
        checkDatabaseMetadataAgainstFile("vartypes_h2/h2vartypes.db", "h2:file", exposedTableFilename, tableName, "vartypes_h2")
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
}