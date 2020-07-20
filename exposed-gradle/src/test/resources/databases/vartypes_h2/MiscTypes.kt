package databases.vartypes_h2

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import java.util.UUID

object MiscTypes : Table("misc_types") {
    val m1: Column<UUID> = uuid("m1")
}