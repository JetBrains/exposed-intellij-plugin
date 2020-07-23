package databases.vartypes_h2

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object DoubleTypes : Table("double_types") {
    val d1: Column<Double> = double("d1")
    val d2: Column<Double> = double("d2")
    val d3: Column<Double> = double("d3")
    val d4: Column<Double> = double("d4")
    val d5: Column<Double> = double("d5")
}