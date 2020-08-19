package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

object MiscTypes : Table("misc_types") {
    val booleanColumn: Column<Boolean> = bool("boolean_column")
    val binaryColumn: Column<ByteArray> = binary("binary_column", 32)
    val blobColumn: Column<ExposedBlob> = blob("blob_column")
}