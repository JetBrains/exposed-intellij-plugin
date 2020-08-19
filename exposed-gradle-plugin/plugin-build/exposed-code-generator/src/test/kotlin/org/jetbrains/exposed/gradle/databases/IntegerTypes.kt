package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object IntegerTypes : Table("integer_types") {
    val tinyIntColumn: Column<Byte> = byte("tiny_int_column")
    val shortColumn: Column<Short> = short("short_column")
    val integerColumn: Column<Int> = integer("integer_column")
    val longColumn: Column<Long> = long("long_column")
}