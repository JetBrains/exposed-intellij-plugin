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

class PostgreSQLTest : ExposedCodeGeneratorFromScriptTest() {
    private fun runPostgreSQLTest(tableName: String, checkTablesBlock: (KotlinCompilation.Result) -> Unit) {
        testByCompilation(
                "vartypes.sql",
                Paths.get(resourcesDatabasesPath.toString(), "psql"),
                checkTablesBlock,
                TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG),
                tableName
        )
    }

    @Test
    fun integerTypes() = runPostgreSQLTest("integer_types") { result ->
        checkTableObject(result, "IntegerTypes", "integer_types", { tableObject ->
            checkColumnProperty(result, tableObject, "i1", "i1", IntegerColumnType(), isAutoIncremented = true)
            checkColumnProperty(result, tableObject, "i2", "i2", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i3", "i3", IntegerColumnType())
            checkColumnProperty(result, tableObject, "i4", "i4", IntegerColumnType())
        })
    }

    @Test
    fun floatingPointTypes() = runPostgreSQLTest("floating_point_types") { result ->
        checkTableObject(result, "FloatingPointTypes", "floating_point_types", { tableObject ->
            checkColumnProperty(result, tableObject, "f1", "f1", DoubleColumnType())
            checkColumnProperty(result, tableObject, "f2", "f2", DoubleColumnType())
            checkColumnProperty(result, tableObject, "f3", "f3", FloatColumnType())
            checkColumnProperty(result, tableObject, "f4", "f4", FloatColumnType())
        })
    }

    @Test
    fun longTypes() = runPostgreSQLTest("long_types") { result ->
        checkTableObject(result, "LongTypes", "long_types", { tableObject ->
            checkColumnProperty(result, tableObject, "l1", "l1", LongColumnType(), isAutoIncremented = true)
            checkColumnProperty(result, tableObject, "l2", "l2", LongColumnType())
        })
    }

    @Test
    fun smallIntTypes() = runPostgreSQLTest("small_int_types") { result ->
        checkTableObject(result, "SmallIntTypes", "small_int_types", { tableObject ->
            checkColumnProperty(result, tableObject, "s1", "s1", ShortColumnType())
            checkColumnProperty(result, tableObject, "s2", "s2", ShortColumnType())
        })
    }

    @Test
    fun decimalTypes() = runPostgreSQLTest("decimal_types") { result ->
        checkTableObject(result, "DecimalTypes", "decimal_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", DecimalColumnType(131072, 0))
            checkColumnProperty(result, tableObject, "d2", "d2", DecimalColumnType(4, 0))
            checkColumnProperty(result, tableObject, "d3", "d3", DecimalColumnType(5, 2))
            checkColumnProperty(result, tableObject, "d4", "d4", DecimalColumnType(131072, 0))
            checkColumnProperty(result, tableObject, "d5", "d5", DecimalColumnType(6, 0))
            checkColumnProperty(result, tableObject, "d6", "d6", DecimalColumnType(7, 3))
        })
    }

    @Test
    fun charTypes() = runPostgreSQLTest("char_types") { result ->
        checkTableObject(result, "CharTypes", "char_types", { tableObject ->
            checkColumnProperty(result, tableObject, "c1", "c1", CharColumnType(5))
            checkColumnProperty(result, tableObject, "c2", "c2", CharColumnType(1))
            checkColumnProperty(result, tableObject, "c3", "c3", VarCharColumnType(2147483647))
            checkColumnProperty(result, tableObject, "c4", "c4", VarCharColumnType(5))
            checkColumnProperty(result, tableObject, "c5", "c5", TextColumnType())
        })
    }

    @Test
    fun datetimeTypes() = runPostgreSQLTest("datetime_types") { result ->
        checkTableObject(result, "DatetimeTypes", "datetime_types", { tableObject ->
            checkColumnProperty(result, tableObject, "d1", "d1", JavaLocalDateTimeColumnType())
            checkColumnProperty(result, tableObject, "d2", "d2", JavaLocalDateColumnType())
        })
    }

    @Test
    fun miscTypes() = runPostgreSQLTest("misc_types") { result ->
        checkTableObject(result, "MiscTypes", "misc_types", { tableObject ->
            checkColumnProperty(result, tableObject, "m1", "m1", BooleanColumnType())
            checkColumnProperty(result, tableObject, "m2", "m2", BinaryColumnType(2147483647))
            checkColumnProperty(result, tableObject, "m3", "m3", UUIDColumnType())
        })
    }
}