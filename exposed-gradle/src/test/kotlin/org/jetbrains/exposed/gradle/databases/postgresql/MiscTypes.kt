package org.jetbrains.exposed.gradle.databases.postgresql

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object MiscTypes : Table("misc_types") {
    val booleanColumn: Column<Boolean> = bool("boolean_column")
    val binaryColumn: Column<ByteArray> = binary("binary_column", 2147483647)
    val blobColumn: Column<ByteArray> = binary("blob_column", 2147483647)
}