object VarcharTypes : Table("varchar_types") {
    val c1: Column<String> = varchar("c1", 2147483647)
    val c2: Column<String> = varchar("c2", 5)
    val c3: Column<String> = varchar("c3", 5)
    val c4: Column<String> = varchar("c4", 2147483647)
    val c5: Column<String> = varchar("c5", 5)
    val c6: Column<String> = varchar("c6", 2147483647)
    val c7: Column<String> = varchar("c7", 5)
    val c8: Column<String> = varchar("c8", 5)
    val c9: Column<String> = varchar("c9", 5)
}