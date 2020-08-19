package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDate
import java.time.LocalDateTime

object DateTimeTypes : Table("date_time_types") {
    val dateColumn: Column<LocalDate> = date("date_column")
    val dateTimeColumn: Column<LocalDateTime> = datetime("date_time_column")
}