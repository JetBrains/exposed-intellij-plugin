package databases.vartypes_psql

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object CharTypes : Table("char_types") {
    val c1: Column<String> = char("c1", 5)
    val c2: Column<String> = char("c2", 1)
    val c3: Column<String> = varchar("c3", 2147483647)
    val c4: Column<String> = varchar("c4", 5)
    val c5: Column<String> = text("c5")

}