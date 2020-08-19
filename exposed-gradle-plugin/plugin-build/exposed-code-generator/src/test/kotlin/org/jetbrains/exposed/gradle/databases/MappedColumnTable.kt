package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object MappedColumnTable : Table("mapped_column_table") {
    val floatColumn: Column<Float> = float("float_column")
    val integerColumn: Column<Int> = integer("integer_column")
}