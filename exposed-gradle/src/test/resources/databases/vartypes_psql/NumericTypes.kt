package databases.vartypes_psql

import org.jetbrains.exposed.sql.*
import java.math.BigDecimal

object NumericTypes : Table("numeric_types") {
    val n1: Column<BigDecimal> = decimal("n1", 65535, 32767)
    val n2: Column<BigDecimal> = decimal("n2", 4, 0)
    val n3: Column<BigDecimal> = decimal("n3", 5, 2)
    val n4: Column<BigDecimal> = decimal("n4", 65535, 32767)
    val n5: Column<BigDecimal> = decimal("n5", 6, 0)
    val n6: Column<BigDecimal> = decimal("n6", 7, 3)
}