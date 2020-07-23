package databases.h2

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDate
import java.time.LocalDateTime

object DatetimeTypes : Table("datetime_types") {
    val d1: Column<LocalDate> = date("d1")
    val d2: Column<LocalDateTime> = datetime("d2")
    val d3: Column<LocalDateTime> = datetime("d3")
}