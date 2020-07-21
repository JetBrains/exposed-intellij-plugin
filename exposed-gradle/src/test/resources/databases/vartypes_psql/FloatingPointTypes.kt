package databases.vartypes_psql

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object FloatingPointTypes : Table("floating_point_types") {
    val f1: Column<Double> = double("f1")
    val f2: Column<Double> = double("f2")
    val f3: Column<Float> = float("f3")
    val f4: Column<Float> = float("f4")
}