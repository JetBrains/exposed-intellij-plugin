object Testrelation : Table("testrelation") {
    val intColumn: Column<Int> = integer("intcolumn")
    val textColumn: Column<String> = text("textcolumn")
    val floatColumn: Column<Double> = double("floatcolumn")
}
