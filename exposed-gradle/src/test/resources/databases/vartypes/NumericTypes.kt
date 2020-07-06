object NumericTypes : Table() {
    val n1: Column<BigDecimal> = decimal("n1", 2000000000, 10)
    val n2: Column<BigDecimal> = decimal("n2", 10, 5)
}