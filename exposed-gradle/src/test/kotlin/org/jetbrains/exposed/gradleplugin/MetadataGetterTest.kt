package org.jetbrains.exposed.gradleplugin

import org.junit.Assert.*
import org.junit.Test
import schemacrawler.schema.Table
import java.io.File
import java.nio.file.Paths

class MetadataGetterTest {
    private fun checkTableMetadataAgainstFile(table: Table, filename: String, vararg fileParentPath: String) {
        val tableExposed = generateExposedTable(table)
        val p = Paths.get("src", "test", "resources", "databases", *fileParentPath)
        val lines = File(p.toFile(), filename).readLines()
        assertEquals(lines, tableExposed)
    }

    private fun getSqliteTablesForDatabase(databaseName: String) = getTables("sqlite", "src/test/resources/databases/$databaseName", "root", "root")

    @Test
    fun oneTableTest() {
        val tables = getSqliteTablesForDatabase("example.db")
        assertEquals(1, tables.size)
        val table = tables[0]
        assertEquals("testrelation", table.name)
        checkTableMetadataAgainstFile(table, "example.kt")
    }

    private fun sqliteTypesTest(tableName: String, exposedTableFilename: String) {
        val tables = getSqliteTablesForDatabase("vartypes/vartypes.db")
        val integerTypesTable = tables.find { it.name == tableName }
        assertNotNull(integerTypesTable)
        checkTableMetadataAgainstFile(integerTypesTable!!, exposedTableFilename, "vartypes")
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
    // in sqlite int8 is somehow integer and not long. unlike in postgresql, where it's long.
    fun sqliteLongTypesTest() {
        sqliteTypesTest("long_types", "LongTypes.kt")
    }

    @Test
    fun sqliteCharTypesTest() {
        sqliteTypesTest("char_types", "CharTypes.kt")
    }

    @Test
    fun intIdTableTest() {
        val tables = getSqliteTablesForDatabase("intid.db")
        assertEquals(1, tables.size)
        val table = tables[0]
        assertEquals("int_id", table.name)
        checkTableMetadataAgainstFile(table, "IntId.kt")
    }
}