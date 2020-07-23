package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.`java-time`.duration
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

object DateTimeTypes : Table("date_time_types") {
    val dateColumn: Column<LocalDate> = date("date_column")
    val dateTimeColumn: Column<LocalDateTime> = datetime("date_time_column")
    val timestampColumn: Column<Instant> = timestamp("timestamp_column")
    val durationColumn: Column<Duration> = duration("duration_column")

    val extraColumn: Column<Long> = long("extra_column")
}