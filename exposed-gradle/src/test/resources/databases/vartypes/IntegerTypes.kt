object IntegerTypes : Table() {
    val i1: Column<Int> = integer("i1")
    val i2: Column<Int> = integer("i2")
    val i3: Column<Int> = integer("i3")
    val i4: Column<Int> = integer("i4").autoIncrement()
}