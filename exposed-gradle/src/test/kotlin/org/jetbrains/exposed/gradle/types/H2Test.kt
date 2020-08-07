package org.jetbrains.exposed.gradle.types

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.gradle.ExposedCodeGeneratorDBTest
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.junit.Test

class H2Test : ExposedCodeGeneratorDBTest("vartypes.sql", "h2", listOf(TestDB.H2, TestDB.H2_MYSQL)) {
    @Test
    fun integerTypes() = runTableTest("integer_types", "IntegerTypes") {
        checkColumnProperty("i1", "i1", IntegerColumnType())
        checkColumnProperty("i2", "i2", IntegerColumnType())
        checkColumnProperty("i3", "i3", IntegerColumnType())
        checkColumnProperty("i4", "i4", IntegerColumnType())
        checkColumnProperty("i5", "i5", IntegerColumnType())
    }

    @Test
    fun floatTypes() = runTableTest("float_types", "FloatTypes") {
        checkColumnProperty("f1", "f1", FloatColumnType())
        checkColumnProperty("f2", "f2", FloatColumnType())
        checkColumnProperty("f3", "f3", FloatColumnType())
    }

    @Test
    fun doubleTypes() = runTableTest("double_types", "DoubleTypes") {
        checkColumnProperty("d1", "d1", DoubleColumnType())
        checkColumnProperty("d2", "d2", DoubleColumnType())
        checkColumnProperty("d3", "d3", DoubleColumnType())
        checkColumnProperty("d4", "d4", DoubleColumnType())
        checkColumnProperty("d5", "d5", DoubleColumnType())
    }

    @Test
    fun booleanTypes() = runTableTest("boolean_types", "BooleanTypes") {
        checkColumnProperty("b1", "b1", BooleanColumnType())
        checkColumnProperty("b2", "b2", BooleanColumnType())
        checkColumnProperty("b3", "b3", BooleanColumnType())
    }

    @Test
    fun longTypes() = runTableTest("long_types") {
        with(TableChecker("LongTypes")) {
            checkTableObject("long_types", {
                checkColumnProperty("l1", "l1", LongColumnType())
                checkColumnProperty("l2", "l2", LongColumnType())
                checkColumnProperty("id", "l3", LongColumnType(), isAutoIncremented = true, isEntityId = true)
            }, tableClass = LongIdTable::class, primaryKeyColumns = listOf("l3"))
        }
    }

    @Test
    fun smallIntTypes() = runTableTest("small_int_types", "SmallIntTypes") {
        checkColumnProperty("t1", "t1", ByteColumnType())
        checkColumnProperty("s1", "s1", ShortColumnType())
        checkColumnProperty("s2", "s2", ShortColumnType())
        checkColumnProperty("s3", "s3", ShortColumnType())
    }

    @Test
    fun decimalTypes() = runTableTest("decimal_types", "DecimalTypes") {
        checkColumnProperty("d1", "d1", DecimalColumnType(10, 0))
        checkColumnProperty("d2", "d2", DecimalColumnType(10, 5))
        checkColumnProperty("d3", "d3", DecimalColumnType(5, 0))
        checkColumnProperty("d4", "d4", DecimalColumnType(15, 10))
        checkColumnProperty("d5", "d5", DecimalColumnType(3, 0))
        checkColumnProperty("d6", "d6", DecimalColumnType(3, 2))
        checkColumnProperty("d7", "d7", DecimalColumnType(4, 0))
        checkColumnProperty("d8", "d8", DecimalColumnType(4, 2))
    }

    @Test
    fun charTypes() = runTableTest("char_types", "CharTypes") {
        checkColumnProperty("c1", "c1", CharColumnType(2147483647))
        checkColumnProperty("c2", "c2", CharColumnType(5))
        checkColumnProperty("c3", "c3", CharColumnType(5))
        checkColumnProperty("c4", "c4", CharColumnType(5))
    }

    @Test
    fun varcharTypes() = runTableTest("varchar_types", "VarcharTypes") {
        checkColumnProperty("c1", "c1", VarCharColumnType(2147483647))
        checkColumnProperty("c2", "c2", VarCharColumnType(5))
        checkColumnProperty("c3", "c3", VarCharColumnType(5))
        checkColumnProperty("c4", "c4", VarCharColumnType(2147483647))
        checkColumnProperty("c5", "c5", VarCharColumnType(5))
        checkColumnProperty("c6", "c6", VarCharColumnType(2147483647))
        checkColumnProperty("c7", "c7", VarCharColumnType(5))
        checkColumnProperty("c8", "c8", VarCharColumnType(5))
        checkColumnProperty("c9", "c9", VarCharColumnType(5))
    }

    @Test
    fun textTypes() = runTableTest("text_types", "TextTypes") {
        checkColumnProperty("t1", "t1", TextColumnType())
        checkColumnProperty("t2", "t2", TextColumnType())
        checkColumnProperty("t3", "t3", TextColumnType())
        checkColumnProperty("t4", "t4", TextColumnType())
        checkColumnProperty("t5", "t5", TextColumnType())
        checkColumnProperty("t6", "t6", TextColumnType())
        checkColumnProperty("t7", "t7", TextColumnType())
        checkColumnProperty("t8", "t8", TextColumnType())
        checkColumnProperty("t9", "t9", TextColumnType())
    }

    @Test
    fun binaryTypes() = runTableTest("binary_types", "BinaryTypes") {
        checkColumnProperty("b1", "b1", BlobColumnType())
        checkColumnProperty("b2", "b2", BlobColumnType())
        checkColumnProperty("b3", "b3", BinaryColumnType(2147483647))
        checkColumnProperty("b4", "b4", BinaryColumnType(32))
        checkColumnProperty("b5", "b5", BinaryColumnType(2147483647))
    }

    @Test
    fun datetimeTypes() = runTableTest("datetime_types", "DatetimeTypes") {
        checkColumnProperty("d1", "d1", JavaLocalDateColumnType())
        checkColumnProperty("d2", "d2", JavaLocalDateTimeColumnType())
        checkColumnProperty("d3", "d3", JavaLocalDateTimeColumnType())
    }

    @Test
    fun miscTypes() = runTableTest("misc_types", "MiscTypes") {
        checkColumnProperty("m1", "m1", UUIDColumnType())
    }

    @Test
    fun uniqueIndex() = runTableTest("index_table") {
        with(TableChecker("IndexTable")) {
            checkTableObject("index_table", {
                checkColumnProperty("i1", "i1", IntegerColumnType())
                checkColumnProperty("i2", "i2", IntegerColumnType())
                checkColumnProperty("i3", "i3", IntegerColumnType())
            }, indexes = listOf(CompilationResultChecker.IndexWrapper("idx1", true, setOf("i1", "i3"))))
        }
    }
}
