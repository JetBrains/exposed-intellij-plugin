package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object CharTypes : Table("char_types") {
    val charColumn: Column<String> = char("char_column", 5)
    val varcharColumn: Column<String> = varchar("varchar_column", 5)
    val textColumn: Column<String> = text("text_column")
}