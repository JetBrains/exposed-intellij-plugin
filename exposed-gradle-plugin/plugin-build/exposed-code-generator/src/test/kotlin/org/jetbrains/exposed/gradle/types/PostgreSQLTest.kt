package org.jetbrains.exposed.gradle.types

import org.jetbrains.exposed.gradle.ExposedCodeGeneratorDBTest
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.javatime.JavaLocalDateTimeColumnType
import org.junit.Test

class PostgreSQLTest : ExposedCodeGeneratorDBTest("vartypes.sql", "psql", listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)) {
    @Test
    fun integerTypes() = runTableTest("integer_types", "IntegerTypes") {
        checkColumnProperty("i1", "i1", IntegerColumnType(), isAutoIncremented = true)
        checkColumnProperty("i2", "i2", IntegerColumnType())
        checkColumnProperty("i3", "i3", IntegerColumnType())
        checkColumnProperty("i4", "i4", IntegerColumnType())
    }

    @Test
    fun floatingPointTypes() = runTableTest("floating_point_types", "FloatingPointTypes") {
        checkColumnProperty("f1", "f1", DoubleColumnType())
        checkColumnProperty("f2", "f2", DoubleColumnType())
        checkColumnProperty("f3", "f3", FloatColumnType())
        checkColumnProperty("f4", "f4", FloatColumnType())
    }

    @Test
    fun longTypes() = runTableTest("long_types", "LongTypes") {
        checkColumnProperty("l1", "l1", LongColumnType(), isAutoIncremented = true)
        checkColumnProperty("l2", "l2", LongColumnType())
    }

    @Test
    fun smallIntTypes() = runTableTest("small_int_types", "SmallIntTypes") {
        checkColumnProperty("s1", "s1", ShortColumnType())
        checkColumnProperty("s2", "s2", ShortColumnType())
    }

    @Test
    fun decimalTypes() = runTableTest("decimal_types", "DecimalTypes") {
        checkColumnProperty("d1", "d1", DecimalColumnType(131072, 0))
        checkColumnProperty("d2", "d2", DecimalColumnType(4, 0))
        checkColumnProperty("d3", "d3", DecimalColumnType(5, 2))
        checkColumnProperty("d4", "d4", DecimalColumnType(131072, 0))
        checkColumnProperty("d5", "d5", DecimalColumnType(6, 0))
        checkColumnProperty("d6", "d6", DecimalColumnType(7, 3))
    }

    @Test
    fun charTypes() = runTableTest("char_types", "CharTypes") {
        checkColumnProperty("c1", "c1", CharColumnType(5))
        checkColumnProperty("c2", "c2", CharColumnType(1))
        checkColumnProperty("c3", "c3", VarCharColumnType(2147483647))
        checkColumnProperty("c4", "c4", VarCharColumnType(5))
        checkColumnProperty("c5", "c5", TextColumnType())
    }

    @Test
    fun datetimeTypes() = runTableTest("datetime_types", "DatetimeTypes") {
        checkColumnProperty("d1", "d1", JavaLocalDateTimeColumnType())
        checkColumnProperty("d2", "d2", JavaLocalDateColumnType())
    }

    @Test
    fun miscTypes() = runTableTest("misc_types", "MiscTypes") {
        checkColumnProperty("m1", "m1", BooleanColumnType())
        checkColumnProperty("m2", "m2", BinaryColumnType(2147483647))
        checkColumnProperty("m3", "m3", UUIDColumnType())
    }
}
