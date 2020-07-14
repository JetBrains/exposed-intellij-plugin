package databases.vartypes_psql

import org.jetbrains.exposed.sql.*
import java.util.*

object MiscTypes : Table("misc_types") {
    val m1: Column<Boolean> = bool("m1")
    val m2: Column<ByteArray> = binary("m2", 2147483647)
    val m3: Column<UUID> = uuid("m3")
}