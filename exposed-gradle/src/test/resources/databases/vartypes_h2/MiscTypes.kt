object MiscTypes : Table("misc_types") {
    val m1: Column<ExposedBlob> = blob("m1")
    val m2: Column<ExposedBlob> = blob("m2")
    val m3: Column<UUID> = uuid("m3")
}