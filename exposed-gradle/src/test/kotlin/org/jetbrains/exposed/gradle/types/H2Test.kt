package org.jetbrains.exposed.gradle.types

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.gradle.ExposedCodeGeneratorFromScriptTest
import org.jetbrains.exposed.gradle.resourcesDatabasesPath
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.junit.Test
import java.nio.file.Paths

class H2Test : ExposedCodeGeneratorFromScriptTest() {
    private fun runH2Test(tableName: String, checkTablesBlock: (KotlinCompilation.Result) -> Unit) {
        testByCompilation(
                "vartypes.sql",
                Paths.get(resourcesDatabasesPath.toString(), "h2"),
                checkTablesBlock,
                TestDB.enabledInTests() - listOf(TestDB.H2, TestDB.H2_MYSQL),
                tableName
        )
    }

    @Test
    fun integerTypes() = runH2Test("integer_types") { result ->
        checkTableObject(result, "IntegerTypes", "integer_types", { tableObject ->
            checkColumnProperty(result, tableObject, "i1", "i1", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i2", "i2", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i3", "i3", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i4", "i4", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i5", "i5", IntegerColumnType())
        })
    }

    @Test
    fun floatTypes() = runH2Test("float_types") { result ->
        checkTableObject(result, "FloatTypes", "float_types", { tableObject ->
            checkColumnProperty(result, tableObject, "f1", "f1", FloatColumnType())
            checkColumnProperty(result, tableObject, "f2", "f2", FloatColumnType())
            checkColumnProperty(result, tableObject, "f3", "f3", FloatColumnType())
        })
    }

    @Test
    fun doubleTypes() = runH2Test("double_types") { result ->
        checkTableObject(result, "DoubleTypes", "double_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", DoubleColumnType())
            checkColumnProperty(result, tableObject, "d2", "d2", DoubleColumnType())
            checkColumnProperty(result, tableObject, "d3", "d3", DoubleColumnType())
            checkColumnProperty(result, tableObject, "d4", "d4", DoubleColumnType())
            checkColumnProperty(result, tableObject, "d5", "d5", DoubleColumnType())
        })
    }

    @Test
    fun booleanTypes() = runH2Test("boolean_types") { result ->
        checkTableObject(result, "BooleanTypes", "boolean_types", { tableObject ->
            checkColumnProperty(result, tableObject, "b1", "b1", BooleanColumnType())
            checkColumnProperty(result, tableObject, "b2", "b2", BooleanColumnType())
            checkColumnProperty(result, tableObject, "b3", "b3", BooleanColumnType())
        })
    }

    @Test
    fun longTypes() = runH2Test("long_types") { result ->
        checkTableObject(result, "LongTypes", "long_types", { tableObject ->
            checkColumnProperty(result, tableObject, "l1", "l1", LongColumnType())
            checkColumnProperty(result, tableObject, "l2", "l2", LongColumnType())
            checkColumnProperty(result, tableObject, "id", "l3", LongColumnType(), isAutoIncremented = true, isEntityId = true)
        }, tableClass = LongIdTable::class, primaryKeyColumns = listOf("l3"))
    }
    @Test
    fun smallIntTypes() = runH2Test("small_int_types") { result ->
        checkTableObject(result, "SmallIntTypes", "small_int_types", { tableObject ->
            checkColumnProperty(result, tableObject, "t1", "t1", ByteColumnType())
            checkColumnProperty(result, tableObject, "s1", "s1", ShortColumnType())
            checkColumnProperty(result, tableObject, "s2", "s2", ShortColumnType())
            checkColumnProperty(result, tableObject, "s3", "s3", ShortColumnType())
        })
    }

    @Test
    fun decimalTypes() = runH2Test("decimal_types") { result ->
        checkTableObject(result, "DecimalTypes", "decimal_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", DecimalColumnType(10, 0))
            checkColumnProperty(result, tableObject, "d2", "d2", DecimalColumnType(10, 5))
            checkColumnProperty(result, tableObject, "d3", "d3", DecimalColumnType(5, 0))
            checkColumnProperty(result, tableObject, "d4", "d4", DecimalColumnType(15, 10))
            checkColumnProperty(result, tableObject, "d5", "d5", DecimalColumnType(3, 0))
            checkColumnProperty(result, tableObject, "d6", "d6", DecimalColumnType(3, 2))
            checkColumnProperty(result, tableObject, "d7", "d7", DecimalColumnType(4, 0))
            checkColumnProperty(result, tableObject, "d8", "d8", DecimalColumnType(4, 2))
        })
    }

    @Test
    fun charTypes() = runH2Test("char_types") { result ->
        checkTableObject(result, "CharTypes", "char_types", { tableObject ->
            checkColumnProperty(result, tableObject, "c1", "c1", CharColumnType(2147483647))
            checkColumnProperty(result, tableObject, "c2", "c2", CharColumnType(5))
            checkColumnProperty(result, tableObject, "c3", "c3", CharColumnType(5))
            checkColumnProperty(result, tableObject, "c4", "c4", CharColumnType(5))
        })
    }

    @Test
    fun varcharTypes() = runH2Test("varchar_types") { result ->
        checkTableObject(result, "VarcharTypes", "varchar_types", { tableObject ->
            checkColumnProperty(result, tableObject, "c1", "c1", VarCharColumnType(2147483647))
            checkColumnProperty(result, tableObject, "c2", "c2", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c3", "c3", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c4", "c4", VarCharColumnType(2147483647))
            checkColumnProperty(result, tableObject, "c5", "c5", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c6", "c6", VarCharColumnType(2147483647))
            checkColumnProperty(result, tableObject, "c7", "c7", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c8", "c8", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c9", "c9", VarCharColumnType(5))
        })
    }

    @Test
    fun textTypes() = runH2Test("text_types") { result ->
        checkTableObject(result, "TextTypes", "text_types", { tableObject ->
            checkColumnProperty(result, tableObject, "t1", "t1", TextColumnType())
            checkColumnProperty(result, tableObject, "t2", "t2", TextColumnType())
            checkColumnProperty(result, tableObject, "t3", "t3", TextColumnType())
            checkColumnProperty(result, tableObject, "t4", "t4", TextColumnType())
            checkColumnProperty(result, tableObject, "t5", "t5", TextColumnType())
            checkColumnProperty(result, tableObject, "t6", "t6", TextColumnType())
            checkColumnProperty(result, tableObject, "t7", "t7", TextColumnType())
            checkColumnProperty(result, tableObject, "t8", "t8", TextColumnType())
            checkColumnProperty(result, tableObject, "t9", "t9", TextColumnType())
        })
    }

    @Test
    fun binaryTypes() = runH2Test("binary_types") { result ->
        checkTableObject(result, "BinaryTypes", "binary_types", { tableObject ->
            checkColumnProperty(result, tableObject, "b1", "b1", BlobColumnType())
            checkColumnProperty(result, tableObject, "b2", "b2", BlobColumnType())
            checkColumnProperty(result, tableObject, "b3", "b3", BinaryColumnType(2147483647))
            checkColumnProperty(result, tableObject, "b4", "b4", BinaryColumnType(32))
            checkColumnProperty(result, tableObject, "b5", "b5", BinaryColumnType(2147483647))
        })
    }

    @Test
    fun datetimeTypes() = runH2Test("datetime_types") { result ->
        checkTableObject(result, "DatetimeTypes", "datetime_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", JavaLocalDateColumnType())
            checkColumnProperty(result, tableObject, "d2", "d2", JavaLocalDateTimeColumnType())
            checkColumnProperty(result, tableObject, "d3", "d3", JavaLocalDateTimeColumnType())
        })
    }

    @Test
    fun miscTypes() = runH2Test("misc_types") { result ->
        checkTableObject(result, "MiscTypes", "misc_types", { tableObject ->
            checkColumnProperty(result, tableObject, "m1", "m1", UUIDColumnType())
        })
    }
}