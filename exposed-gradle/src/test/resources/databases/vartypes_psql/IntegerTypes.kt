package databases.vartypes_psql

import org.jetbrains.exposed.sql.*

object IntegerTypes : Table("integer_types") {
    val i1: Column<Int> = integer("i1").autoIncrement()
    val i2: Column<Int> = integer("i2")
    val i3: Column<Int> = integer("i3")
    val i4: Column<Int> = integer("i4")
}