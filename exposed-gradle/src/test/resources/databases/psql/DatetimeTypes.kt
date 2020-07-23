package databases.psql

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant
import java.time.LocalDate

object DatetimeTypes : Table("datetime_types") {
    val d1: Column<Instant> = timestamp("d1")
    val d2: Column<LocalDate> = date("d2")
}