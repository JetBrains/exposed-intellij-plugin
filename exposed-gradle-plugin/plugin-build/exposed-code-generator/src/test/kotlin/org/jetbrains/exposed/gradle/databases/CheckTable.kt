package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Table

object CheckTable : Table("check_table") {
    val c1 = integer("c1").check { it.between(20, 40) }
    val c2 = integer("c2").check { it.eq(30) }
    val c3 = integer("c3").check { it.neq(40) }
    val c4 = integer("c4").check { it.greater(10) }
}