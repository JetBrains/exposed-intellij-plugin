package databases.vartypes_h2

import org.jetbrains.exposed.sql.*

object SmallIntTypes : Table("small_int_types") {
    val t1: Column<Byte> = byte("t1")
    val s1: Column<Short> = short("s1")
    val s2: Column<Short> = short("s2")
    val s3: Column<Short> = short("s3")
}