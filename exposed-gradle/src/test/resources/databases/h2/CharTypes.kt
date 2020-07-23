package databases.vartypes_h2

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object CharTypes : Table("char_types") {
    val c1: Column<String> = char("c1", 2147483647)
    val c2: Column<String> = char("c2", 5)
    val c3: Column<String> = char("c3", 5)
    val c4: Column<String> = char("c4", 5)
}