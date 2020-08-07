package org.jetbrains.exposed.gradle.databases

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Sample : Table("sample") {
    val c1: Column<Int> = integer("c1").uniqueIndex()
    val c2: Column<String> = text("c2")
}

object SampleRef : Table("sample_ref") {
    val c1: Column<Int> = integer("c1")
    val c2: Column<Int> = integer("c2").references(Sample.c1)
}