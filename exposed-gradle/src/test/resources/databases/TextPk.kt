object TextPk : IdTable<String>("text_pk", "column_1") {
    val column2: Column<Int> = integer("column_2")
}