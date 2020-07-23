package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

object DecimalTypes : Table("decimal_types") {
    val n1: Column<BigDecimal> = decimal("n1", 131072, 10)
    val n2: Column<BigDecimal> = decimal("n2", 10, 5)
}