package databases.vartypes_h2

import org.jetbrains.exposed.sql.*
import java.util.*

object MiscTypes : Table("misc_types") {
    val m1: Column<UUID> = uuid("m1")
}