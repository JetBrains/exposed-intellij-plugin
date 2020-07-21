package databases.vartypes_psql

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object SmallIntTypes : Table("small_int_types") {
    val s1: Column<Short> = short("s1")
    val s2: Column<Short> = short("s2")
}