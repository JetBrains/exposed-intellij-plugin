package org.jetbrains.exposed.gradle.types

import org.jetbrains.exposed.gradle.ExposedCodeGeneratorDBTest
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.junit.Test

class SQLiteTest : ExposedCodeGeneratorDBTest("vartypes.sql", "sqlite", listOf(TestDB.SQLITE)) {
    @Test
    fun integerTypes() = runTableTest("integer_types", "IntegerTypes") {
        checkColumnProperty("i1", "i1", IntegerColumnType())
        checkColumnProperty("i2", "i2", IntegerColumnType())
        checkColumnProperty("i3", "i3", IntegerColumnType())
        checkColumnProperty("i4", "i4", ByteColumnType())
        checkColumnProperty("i5", "i5", ShortColumnType())
        checkColumnProperty("i6", "i6", ShortColumnType())
    }

    @Test
    fun floatingPointTypes() = runTableTest("floating_point_types", "FloatingPointTypes") {
        checkColumnProperty("f1", "f1", FloatColumnType())
        checkColumnProperty("f2", "f2", DoubleColumnType())
        checkColumnProperty("f3", "f3", DoubleColumnType())
        checkColumnProperty("f4", "f4", DoubleColumnType())
    }

    @Test
    fun longTypes() = runTableTest("long_types", "LongTypes") {
        checkColumnProperty("l1", "l1", LongColumnType())
        checkColumnProperty("l2", "l2", LongColumnType())
    }

    @Test
    fun decimalTypes() = runTableTest("decimal_types", "DecimalTypes") {
        checkColumnProperty("d1", "d1", DecimalColumnType(131072, 10))
        checkColumnProperty("d2", "d2", DecimalColumnType(10, 5))
    }

    @Test
    fun charTypes() = runTableTest("char_types", "CharTypes") {
        checkColumnProperty("c1", "c1", CharColumnType(19))
        checkColumnProperty("c2", "c2", VarCharColumnType(255))
        checkColumnProperty("c3", "c3", VarCharColumnType(255))
        checkColumnProperty("c4", "c4", TextColumnType())
    }

    @Test
    fun datetimeTypes() = runTableTest("datetime_types", "DatetimeTypes") {
        checkColumnProperty("d1", "d1", JavaLocalDateTimeColumnType())
        checkColumnProperty("d2", "d2", JavaLocalDateColumnType())
    }

    @Test
    fun miscTypes() = runTableTest("misc_types", "MiscTypes") {
        checkColumnProperty("b1", "b1", BooleanColumnType())
        checkColumnProperty("b2", "b2", BlobColumnType())
    }
}
