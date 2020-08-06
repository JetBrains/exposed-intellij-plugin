package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object HashIndexTable : Table("hash_index_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<String> = text("c2")
    init {
        index("custom_index_name", false, c1, indexType = "HASH")
    }
}

object IndexTable : Table("index_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<Int> = integer("c2").index("idx2")
    init {
        index("idx1", false, c1)
    }
}

object MultiColumnIndexTable : Table("multi_column_index_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<String> = text("c2")
    val c3: Column<Int> = integer("c3").uniqueIndex("custom_unique_index_name")
    init {
        index("custom_index_name", false, c1, c3)
    }
}

object UniqueIndexTable : Table("unique_index_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<Int> = integer("c2").uniqueIndex("idx1")
    init {
        uniqueIndex("idx2", c1, c2)
    }
}