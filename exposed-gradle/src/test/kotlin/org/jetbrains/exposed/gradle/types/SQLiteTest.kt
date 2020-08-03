package org.jetbrains.exposed.gradle.types

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.exposed.gradle.ExposedCodeGeneratorFromScriptTest
import org.jetbrains.exposed.gradle.resourcesDatabasesPath
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.junit.Test
import java.nio.file.Paths

class SQLiteTest : ExposedCodeGeneratorFromScriptTest() {
    private fun runSQLiteTest(tableName: String, checkTablesBlock: (KotlinCompilation.Result) -> Unit) {
        testByCompilation(
                "vartypes.sql",
                Paths.get(resourcesDatabasesPath.toString(), "sqlite"),
                checkTablesBlock,
                TestDB.enabledInTests() - listOf(TestDB.SQLITE),
                tableName
        )
    }

    @Test
    fun integerTypes() = runSQLiteTest("integer_types") { result ->
        checkTableObject(result, "IntegerTypes", "integer_types", { tableObject ->
            checkColumnProperty(result, tableObject, "i1", "i1", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i2", "i2", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i3", "i3", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i4", "i4", ByteColumnType())
            checkColumnProperty(result, tableObject, "i5", "i5", ShortColumnType())
            checkColumnProperty(result, tableObject, "i6", "i6", ShortColumnType())
        })
    }

    @Test
    fun floatingPointTypes() = runSQLiteTest("floating_point_types") { result ->
        checkTableObject(result, "FloatingPointTypes", "floating_point_types", { tableObject ->
            checkColumnProperty(result, tableObject, "f1", "f1", FloatColumnType())
            checkColumnProperty(result, tableObject, "f2", "f2", DoubleColumnType())
            checkColumnProperty(result, tableObject, "f3", "f3", DoubleColumnType())
            checkColumnProperty(result, tableObject, "f4", "f4", DoubleColumnType())
        })
    }

    @Test
    fun longTypes() = runSQLiteTest("long_types") { result ->
        checkTableObject(result, "LongTypes", "long_types", { tableObject ->
            checkColumnProperty(result, tableObject, "l1", "l1", LongColumnType())
            checkColumnProperty(result, tableObject, "l2", "l2", LongColumnType())
        })
    }

    @Test
    fun decimalTypes() = runSQLiteTest("decimal_types") { result ->
        checkTableObject(result, "DecimalTypes", "decimal_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", DecimalColumnType(131072, 10))
            checkColumnProperty(result, tableObject, "d2", "d2", DecimalColumnType(10, 5))
        })
    }

    @Test
    fun charTypes() = runSQLiteTest("char_types") { result ->
        checkTableObject(result, "CharTypes", "char_types", { tableObject ->
            checkColumnProperty(result, tableObject, "c1", "c1", CharColumnType(19))
            checkColumnProperty(result, tableObject, "c2", "c2", VarCharColumnType(255))
            checkColumnProperty(result, tableObject, "c3", "c3", VarCharColumnType(255))
            checkColumnProperty(result, tableObject, "c4", "c4", TextColumnType())
        })
    }

    @Test
    fun datetimeTypes() = runSQLiteTest("datetime_types") { result ->
        checkTableObject(result, "DatetimeTypes", "datetime_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", JavaLocalDateTimeColumnType())
            checkColumnProperty(result, tableObject, "d2", "d2", JavaLocalDateColumnType())
        })
    }

    @Test
    fun miscTypes() = runSQLiteTest("misc_types") { result ->
        checkTableObject(result, "MiscTypes", "misc_types", { tableObject ->
            checkColumnProperty(result, tableObject, "b1", "b1", BooleanColumnType())
            checkColumnProperty(result, tableObject, "b2", "b2", BlobColumnType())
        })
    }
}