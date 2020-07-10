package databases.vartypes_h2

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*

object LongTypes : LongIdTable("long_types", "l3") {
    val l1: Column<Long> = long("l1")
    val l2: Column<Long> = long("l2")
}