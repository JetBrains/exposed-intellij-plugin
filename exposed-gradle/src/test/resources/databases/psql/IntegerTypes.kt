package databases.vartypes_psql

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object IntegerTypes : Table("integer_types") {
    val i1: Column<Int> = integer("i1").autoIncrement()
    val i2: Column<Int> = integer("i2")
    val i3: Column<Int> = integer("i3")
    val i4: Column<Int> = integer("i4")
}