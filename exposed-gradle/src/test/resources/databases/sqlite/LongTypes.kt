package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object LongTypes : Table("long_types") {
    val l1: Column<Long> = long("l1")
    val l2: Column<Long> = long("l2")
}