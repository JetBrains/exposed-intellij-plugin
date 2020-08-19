package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object AutoIncrementTable : Table("auto_increment_table") {
    val c1: Column<Int> = integer("c1").autoIncrement()
    val c2: Column<Long> = long("c2").autoIncrement()
}