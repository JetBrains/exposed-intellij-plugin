package databases.vartypes_h2

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

object DecimalTypes : Table("decimal_types") {
    val d1: Column<BigDecimal> = decimal("d1", 10, 0)
    val d2: Column<BigDecimal> = decimal("d2", 10, 5)
    val d3: Column<BigDecimal> = decimal("d3", 5, 0)
    val d4: Column<BigDecimal> = decimal("d4", 15, 10)
    val d5: Column<BigDecimal> = decimal("d5", 3, 0)
    val d6: Column<BigDecimal> = decimal("d6", 3, 2)
    val d7: Column<BigDecimal> = decimal("d7", 4, 0)
    val d8: Column<BigDecimal> = decimal("d8", 4, 2)
}