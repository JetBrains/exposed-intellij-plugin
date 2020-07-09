object BooleanTypes : Table("boolean_types") {
    val b1: Column<Boolean> = bool("b1")
    val b2: Column<Boolean> = bool("b2")
    val b3: Column<Boolean> = bool("b3")
}