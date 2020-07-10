package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.*

object LongTypes : Table("long_types") {
    val l1: Column<Long> = long("l1")
    val l2: Column<Long> = long("l2")
}