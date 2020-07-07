object Testrelation : Table() {
    val intColumn: Column<Int> = integer("intColumn")
    val textColumn: Column<String> = text("textColumn")
    val floatColumn: Column<Double> = double("floatColumn")
}
