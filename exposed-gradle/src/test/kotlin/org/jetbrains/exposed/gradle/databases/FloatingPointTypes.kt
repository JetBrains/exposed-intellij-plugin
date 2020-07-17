package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object FloatingPointTypes : Table("floating_point_types") {
    val floatColumn: Column<Float> = float("float_column")
    val doubleColumn: Column<Double> = double("double_column")
}