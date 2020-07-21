package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object RefTable : Table("ref_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<Int> = integer("c2").references(c1)
    val c3: Column<Int> = integer("c3")
}