package databases

import org.jetbrains.exposed.sql.*

object RefTable : Table("ref_table") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<String> = text("c2")
    val c3: Column<Int> = integer("c3").references(c1)
}

object Sample : Table("sample") {
    val i1: Column<Int> = integer("i1")
    val c1: Column<String> = text("c1")
}

object RefTable2 : Table("ref_table_2") {
    val ref1: Column<Int> = integer("ref_1").references(RefTable.c1)
    val c1: Column<Int> = integer("c1")
    val c2: Column<String> = text("c2")
}

object SampleRef : Table("sample_ref") {
    val sRef: Column<Int> = integer("s_ref").references(Sample.i1)
    val i1: Column<Int> = integer("i1")
}

object DoubleRef : Table("double_ref") {
    val ref1: Column<Int> = integer("ref_1").references(RefTable2.ref1)
    val ref2: Column<Int> = integer("ref_2").references(RefTable2.c1)
    val ref3: Column<Int> = integer("ref_3").references(SampleRef.sRef)
    val ref4: Column<Int> = integer("ref_4").references(SampleRef.i1)
}
