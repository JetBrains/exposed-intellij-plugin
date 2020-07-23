package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object IntegerTypes : Table("integer_types") {
    val i1: Column<Int> = integer("i1")
    val i2: Column<Int> = integer("i2")
    val i3: Column<Int> = integer("i3")
    val i4: Column<Byte> = byte("i4")
    val i5: Column<Short> = short("i5")
    val i6: Column<Short> = short("i6")
}