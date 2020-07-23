package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object CharTypes : Table("char_types") {
    val ch1: Column<String> = char("ch1", 19)
    val ch2: Column<String> = varchar("ch2", 255)
    val ch3: Column<String> = varchar("ch3", 255)
    val ch4: Column<String> = text("ch4")
}