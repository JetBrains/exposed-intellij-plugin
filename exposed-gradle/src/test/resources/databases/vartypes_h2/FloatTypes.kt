package databases.vartypes_h2

import org.jetbrains.exposed.sql.*

object FloatTypes : Table("float_types") {
    val f1: Column<Float> = float("f1")
    val f2: Column<Float> = float("f2")
    val f3: Column<Float> = float("f3")
}