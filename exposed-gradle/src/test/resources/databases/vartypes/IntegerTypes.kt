object IntegerTypes : Table() {
    val i1: Column<Int> = integer("i1")
    val i2: Column<Int> = integer("i2")
    val i3: Column<Int> = integer("i3").autoIncrement()
    val i4: Column<Int> = integer("i4")
    val i5: Column<Byte> = byte("i5")
    val i6: Column<Short> = short("i6")
    val i7: Column<Short> = short("i7")
}