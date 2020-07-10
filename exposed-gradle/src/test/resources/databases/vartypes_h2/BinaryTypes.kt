package databases.vartypes_h2

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

object BinaryTypes : Table("binary_types") {
    val b1: Column<ExposedBlob> = blob("b1")
    val b2: Column<ExposedBlob> = blob("b2")
    val b3: Column<ByteArray> = binary("b3", 2147483647)
    val b4: Column<ByteArray> = binary("b4", 32)
    val b5: Column<ByteArray> = binary("b5", 2147483647)
}