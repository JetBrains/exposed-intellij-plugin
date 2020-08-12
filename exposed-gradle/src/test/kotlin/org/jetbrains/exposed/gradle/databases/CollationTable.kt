package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Table

object CollationTableSQLite : Table("collation_table_sqlite") {
    val c1 = text("c1", "NOCASE")
    val c2 = char("c2", 30, "NOCASE")
    val c3 = varchar("c3", 30, "NOCASE")
}

object CollationTablePostgreSQL : Table("collation_table_postgresql") {
    val c1 = text("c1", "pg_catalog.\"default\"")
    val c2 = char("c2", 30, "pg_catalog.\"default\"")
    val c3 = varchar("c3", 30, "pg_catalog.\"default\"")
}