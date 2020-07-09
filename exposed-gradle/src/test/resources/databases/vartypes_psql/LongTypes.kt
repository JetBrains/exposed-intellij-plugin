object LongTypes : Table("long_types") {
    val l1: Column<Long> = long("l1").autoIncrement()
    val l2: Column<Long> = long("l2")
}