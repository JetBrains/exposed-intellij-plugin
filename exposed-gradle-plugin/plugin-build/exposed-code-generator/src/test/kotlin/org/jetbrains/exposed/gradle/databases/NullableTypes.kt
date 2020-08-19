package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object NullableTypes : Table("nullable_types") {
    val c1: Column<Int?> = integer("c1").nullable()
    val c2: Column<Long?> = long("c2").nullable()
    val c3: Column<Double?> = double("c3").nullable()
    val c4: Column<String?> = char("c4", 5).nullable()
    val c5: Column<Boolean?> = bool("c5").nullable()
}