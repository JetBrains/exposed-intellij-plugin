package databases.vartypes_h2

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

object TextTypes : Table("text_types") {
    val t1: Column<String> = text("t1")
    val t2: Column<String> = text("t2")
    val t3: Column<String> = text("t3")
    val t4: Column<String> = text("t4")
    val t5: Column<String> = text("t5")
    val t6: Column<String> = text("t6")
    val t7: Column<String> = text("t7")
    val t8: Column<String> = text("t8")
    val t9: Column<String> = text("t9")
}