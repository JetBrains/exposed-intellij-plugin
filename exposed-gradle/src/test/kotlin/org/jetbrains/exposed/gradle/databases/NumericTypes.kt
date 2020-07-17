package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

object NumericTypes : Table("numeric_types") {
    val decimalColumn1: Column<BigDecimal> = decimal("decimal_column_1", 10, 0)
    val decimalColumn2: Column<BigDecimal> = decimal("decimal_column_2", 10, 2)
}