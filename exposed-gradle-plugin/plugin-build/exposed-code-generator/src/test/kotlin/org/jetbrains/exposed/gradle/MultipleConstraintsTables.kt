package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object MultipleConstraintsTable : Table("multiple_constraints_table") {
    val c1: Column<Int> = integer("c1").autoIncrement().uniqueIndex()
    val c2: Column<Int?> = integer("c2").references(c1).nullable()
}