package org.jetbrains.exposed.gradleplugin

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Paths

class MetadataGetterTest {
    @Test
    fun oneTableTest() {
        val tables = getTables("sqlite", "src/test/resources/databases/example.db", "root", "root")
        assertEquals(1, tables.size)
        val table = tables[0]
        assertEquals("testrelation", table.name)
        val tableExposed = generateExposedTable(table)
        val p = Paths.get("src", "test", "resources", "databases")
        val lines = File(p.toFile(), "example.kt").readLines()
        assertEquals(lines, tableExposed)
    }
}