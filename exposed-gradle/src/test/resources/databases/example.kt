object Testrelation : Table() {
    val intcolumn: Column<Int> = integer("intColumn")
    val textcolumn: Column<String> = text("textColumn")
    val floatcolumn: Column<Float> = float("floatColumn")
}
