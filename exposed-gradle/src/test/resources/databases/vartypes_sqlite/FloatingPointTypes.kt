object FloatingPointTypes : Table("floating_point_types") {
    val f1: Column<Float> = float("f1")
    val f2: Column<Double> = double("f2")
    val f3: Column<Double> = double("f3")
    val f4: Column<Double> = double("f4")
}