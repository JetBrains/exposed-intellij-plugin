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
        val fileSpec = generateExposedTablesForDatabase(databaseDriver, "src/test/resources/databases/$databaseName", "root", "root", tableName)
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
        checkDatabaseMetadataAgainstFile("vartypes/vartypes.db", "sqlite", exposedTableFilename, tableName, "vartypes")
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
}