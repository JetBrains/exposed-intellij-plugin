package databases

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object IdPk : IntIdTable("id_pk", "column_1") {
    val column2: Column<String> = text("column_2")
}