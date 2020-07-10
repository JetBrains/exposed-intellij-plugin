package databases

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*

object TextPk : IdTable<String>("text_pk") {
    override val id: Column<EntityID<String>>
        get() = text("column_1").entityId()

    val column2: Column<Int> = integer("column_2")
}