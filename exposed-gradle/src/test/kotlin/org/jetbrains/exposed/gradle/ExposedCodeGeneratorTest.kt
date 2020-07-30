package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.gradle.databases.*
import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.junit.Test
import java.lang.StringBuilder
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class ExposedCodeGeneratorFromScriptTest : DatabaseFromScriptTest() {
    @Test
    // this test can be used for making sure all table references are established correctly
    fun chinook() = testFromScriptAgainstKtFile(
            Paths.get(resourcesTestDataPath.toString(), "sqlite", "chinook.sql"),
            Paths.get("sqlite", "Chinook.kt"),
            excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE)
    )
}


class ExposedCodeGeneratorTestByCompiling : DatabaseTestsBase() {
    private fun compileExposedFile(fileSpec: FileSpec): KotlinCompilation.Result {
        val sb = StringBuilder()
        fileSpec.writeTo(sb)
        val kotlinSource = SourceFile.kotlin(fileSpec.name, sb.toString())
        return KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }

    private fun checkColumnProperty(
            result: KotlinCompilation.Result,
            tableObject: Any,
            columnPropertyName: String,
            columnName: String,
            columnType: ColumnType,
            isNullable: Boolean = false,
            isAutoIncremented: Boolean = false,
            isEntityId: Boolean = false,
            foreignKeyFrom: String? = null,
            foreignKeyTarget: String? = null,
            foreignKeyTargetTable: String? = null
    ) {
        val property = tableObject::class.memberProperties.find { it.name == columnPropertyName } ?: fail("Property $columnPropertyName not found.")

        assertThat(property.returnType.classifier).isEqualTo(Column::class)

        val columnValue = property.getter.call(tableObject)
        val type = (columnValue as Column<*>).columnType
        val name = columnValue.name

        when {
            isEntityId -> {
                assertThat(type).isInstanceOf(EntityIDColumnType::class.java)
                val idColumnType = (type as EntityIDColumnType<*>).idColumn.columnType
                val columnDataType = if (columnType is IntegerColumnType || columnType is LongColumnType) {
                    assertThat(idColumnType).isInstanceOf(AutoIncColumnType::class.java)
                    (idColumnType as AutoIncColumnType).delegate
                } else {
                    idColumnType
                }
                assertThat(columnDataType).isNotInstanceOf(AutoIncColumnType::class.java)
                assertThat(columnDataType).isEqualTo(columnType)
            }
            isAutoIncremented -> {
                assertThat(type).isInstanceOf(AutoIncColumnType::class.java)
                assertThat((type as AutoIncColumnType).delegate).isEqualTo(columnType)
            }
            else -> {
                assertThat(type).isNotInstanceOf(AutoIncColumnType::class.java)
                assertThat(type).isEqualTo(columnType)
            }
        }

        assertThat(type.nullable).isEqualTo(isNullable)

        // assert additional arguments
        // TODO remove once it's fixed in Exposed
        when (type) {
            is CharColumnType -> assertThat(type.colLength).isEqualTo((columnType as CharColumnType).colLength)
        }

        assertThat(name).isEqualTo(columnName)

        if (foreignKeyTarget != null && foreignKeyFrom != null) {
            val foreignKey = columnValue.foreignKey
            assertThat(foreignKey).isNotNull
            assertThat(foreignKey!!.from).isEqualTo(columnValue)
            val foreignKeyTargetTableObject = if (foreignKeyTargetTable == null || foreignKeyTargetTable == tableObject::class.simpleName) {
                tableObject
            } else {
                // TODO a function for that, possibly, because this is the second time i'm using it
                val packageName = if (tableObject::class.java.packageName.isNotBlank()) {
                    "${tableObject::class.java.packageName}."
                } else {
                    ""
                }
                result.classLoader.loadClass("$packageName$foreignKeyTargetTable").kotlin.objectInstance
            }!!
            val target = foreignKeyTargetTableObject::class.memberProperties
                    .find { it.name == foreignKey.targetColumn.toLowerCase() }!!.getter.call(foreignKeyTargetTableObject)
            assertThat(foreignKey.target).isEqualTo(target)
        } else {
            assertThat(columnValue.foreignKey).isNull()
        }
    }

    private fun checkTableObject(
            result: KotlinCompilation.Result,
            tablePropertyName: String,
            tableName: String,
            checkPropertiesBlock: (Any) -> Unit,
            tablePackageName: String = "",
            tableClass: KClass<*> = Table::class,
            primaryKeyColumns: List<String> = emptyList()
    ) {
        val packagePrefix = if (tablePackageName.isNotBlank()) "$tablePackageName." else ""
        val tableObjectClass = result.classLoader.loadClass("$packagePrefix$tablePropertyName").kotlin

        assertThat(tableObjectClass.supertypes).hasSize(1)
        assertThat(tableObjectClass.supertypes[0].classifier).isEqualTo(tableClass)

        val tableObjectInstance = tableObjectClass.objectInstance
        assertThat((tableObjectInstance as Table).tableName).isEqualTo(tableName)

        checkPropertiesBlock(tableObjectInstance)

        if (primaryKeyColumns.isNotEmpty()) {
            assertThat(tableObjectInstance.primaryKey).isNotNull
            assertThat(tableObjectInstance.primaryKey!!.columns).hasSameSizeAs(primaryKeyColumns)
            primaryKeyColumns.forEach { column ->
                assertThat(tableObjectInstance.primaryKey!!.columns).anyMatch { it.name == column }
            }
        } else {
            assertThat(tableObjectInstance.primaryKey).isNull()
        }
    }

    private fun testByCompilation(
            tables: List<Table>,
            checkTablesBlock: (KotlinCompilation.Result) -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null
    ) {
        withDb(excludeSettings = excludedDbList, statement = {
            for (table in tables) {
                SchemaUtils.drop(table)
                SchemaUtils.create(table)
            }

            val fileSpec = getDatabaseExposedFileSpec(it, tableName)
            val result = compileExposedFile(fileSpec)

            // if it didn't compile then there might be imports missing, incorrect types, etc
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlock(result)

            for (table in tables) {
                SchemaUtils.drop(table)
            }
        })
    }


    @Test
    fun charTypes() {
        testByCompilation(listOf(CharTypes), { result ->
            checkTableObject(result, "CharTypes", "char_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "charColumn", "char_column", CharColumnType(5))
                checkColumnProperty(result, tableObjectInstance, "varcharColumn", "varchar_column", VarCharColumnType(5))
                checkColumnProperty(result, tableObjectInstance, "textColumn", "text_column", TextColumnType())
            })
        })
    }

    @Test
    // PostgreSQL doesn't allow for TINYINT columns
    fun integerTypes() {
        testByCompilation(listOf(IntegerTypes), { result ->
            checkTableObject(result, "IntegerTypes", "integer_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "tinyIntColumn", "tiny_int_column", ByteColumnType())
                checkColumnProperty(result, tableObjectInstance, "shortColumn", "short_column", ShortColumnType())
                checkColumnProperty(result, tableObjectInstance, "integerColumn", "integer_column", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "longColumn", "long_column", LongColumnType())
            })
        }, excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgreSQL uses INT2 in place of TINYINT
    fun integerTypesPostgres() {
        testByCompilation(listOf(IntegerTypes), { result ->
            checkTableObject(result, "IntegerTypes", "integer_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "tinyIntColumn", "tiny_int_column", ShortColumnType())
                checkColumnProperty(result, tableObjectInstance, "shortColumn", "short_column", ShortColumnType())
                checkColumnProperty(result, tableObjectInstance, "integerColumn", "integer_column", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "longColumn", "long_column", LongColumnType())
            })
        }, excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgreSQL erases binary columns length
    fun miscTypes() {
        testByCompilation(listOf(MiscTypes), { result ->
            checkTableObject(result, "MiscTypes", "misc_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "booleanColumn", "boolean_column", BooleanColumnType())
                checkColumnProperty(result, tableObjectInstance, "binaryColumn", "binary_column", BinaryColumnType(32))
                checkColumnProperty(result, tableObjectInstance, "blobColumn", "blob_column", BlobColumnType())
            })
        }, excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgresSQL: BLOB -> Binary, binary length is always the maximal possible
    fun miscTypesPostgres() {
        testByCompilation(listOf(MiscTypes), { result ->
            checkTableObject(result, "MiscTypes", "misc_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "booleanColumn", "boolean_column", BooleanColumnType())
                checkColumnProperty(result, tableObjectInstance, "binaryColumn", "binary_column", BinaryColumnType(2147483647))
                checkColumnProperty(result, tableObjectInstance, "blobColumn", "blob_column", BinaryColumnType(2147483647))
            })
        }, excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    fun decimalTypes() {
        testByCompilation(listOf(DecimalTypes), { result ->
            checkTableObject(result, "DecimalTypes", "decimal_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "decimalColumn1", "decimal_column_1", DecimalColumnType(10, 0))
                checkColumnProperty(result, tableObjectInstance, "decimalColumn2", "decimal_column_2", DecimalColumnType(10, 2))
            })
        })
    }

    @Test
    // SQLite stores LocalDateTime as Numeric, making it indistinguishable from actual numeric/decimal columns
    fun dateTimeTypes() {
        testByCompilation(listOf(DateTimeTypes), { result ->
            checkTableObject(result, "DateTimeTypes", "date_time_types", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "dateColumn", "date_column", JavaLocalDateColumnType())
                checkColumnProperty(result, tableObjectInstance, "dateTimeColumn", "date_time_column", JavaLocalDateTimeColumnType())
            })
        }, excludedDbList = listOf(TestDB.SQLITE))
    }

    @Test
    fun nullableTypes() {
        testByCompilation(listOf(NullableTypes), { result ->
            checkTableObject(result, "NullableTypes", "nullable_types",  { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType().apply { nullable = true }, isNullable = true)
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", LongColumnType().apply { nullable = true }, isNullable = true)
                checkColumnProperty(result, tableObjectInstance, "c3", "c3", DoubleColumnType().apply { nullable = true }, isNullable = true)
                checkColumnProperty(result, tableObjectInstance, "c4", "c4", CharColumnType(5).apply { nullable = true }, isNullable = true)
                checkColumnProperty(result, tableObjectInstance, "c5", "c5", BooleanColumnType().apply { nullable = true }, isNullable = true)
            })
        })
    }

    @Test
    fun selfForeignKey() {
        testByCompilation(listOf(SelfForeignKeyTable), { result ->
            checkTableObject(result, "SelfForeignKeyTable", "self_foreign_key_table", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", IntegerColumnType(), foreignKeyFrom = "c2", foreignKeyTarget = "c1")
                checkColumnProperty(result, tableObjectInstance, "c3", "c3", IntegerColumnType())
            })
        }, excludedDbList = listOf(TestDB.MYSQL, TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    fun foreignKey() {
        testByCompilation(listOf(Sample, SampleRef), { result ->
            checkTableObject(result, "Sample", "sample", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", TextColumnType())
            })

            checkTableObject(result, "SampleRef", "sample_ref", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", IntegerColumnType(),
                        foreignKeyFrom = "c2", foreignKeyTarget = "c1", foreignKeyTargetTable = "Sample")
            })
        }, excludedDbList = listOf(TestDB.MYSQL, TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    fun autoIncrement() {
        testByCompilation(listOf(AutoIncrementTable), { result ->
            checkTableObject(result, "AutoIncrementTable", "auto_increment_table", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType(), isAutoIncremented = true)
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", LongColumnType(), isAutoIncremented = true)
            })
        }, excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL))
    }

    @Test
    fun idTables() {
        testByCompilation(listOf(Sample1, Sample2, Sample3, Sample4), { result ->
            checkTableObject(result, "Sample1", "sample_1", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "str", "str", TextColumnType())
                checkColumnProperty(result, tableObjectInstance, "id", "id", IntegerColumnType(), isAutoIncremented = true, isEntityId = true)
            }, tableClass = IntIdTable::class, primaryKeyColumns = listOf("id"))

            checkTableObject(result, "Sample2", "sample_2", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "str", "str", TextColumnType())
                checkColumnProperty(result, tableObjectInstance, "id", "id", LongColumnType(), isAutoIncremented = true, isEntityId = true)
            }, tableClass = LongIdTable::class, primaryKeyColumns = listOf("id"))

            checkTableObject(result, "Sample3", "sample_3", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "str", "str", TextColumnType())
                checkColumnProperty(result, tableObjectInstance, "id", "id", UUIDColumnType(), isEntityId = true)
            }, tableClass = UUIDTable::class, primaryKeyColumns = listOf("id"))

            checkTableObject(result, "Sample4", "sample_4", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "str", "str", TextColumnType())
                checkColumnProperty(result, tableObjectInstance, "id", "id", VarCharColumnType(30), isEntityId = true)
            }, tableClass = IdTable::class, primaryKeyColumns = listOf("id"))
        }, excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL))
    }

    @Test
    fun primaryKey() {
        testByCompilation(listOf(SinglePrimaryKeyTable, CompositePrimaryKeyTable), { result ->
            checkTableObject(result, "SinglePrimaryKeyTable", "single_primary_key_table", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", IntegerColumnType())
            }, primaryKeyColumns = listOf("c1"))

            checkTableObject(result, "CompositePrimaryKeyTable", "composite_primary_key_table", { tableObjectInstance ->
                checkColumnProperty(result, tableObjectInstance, "c1", "c1", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c2", "c2", IntegerColumnType())
                checkColumnProperty(result, tableObjectInstance, "c3", "c3", IntegerColumnType())
            }, primaryKeyColumns = listOf("c1", "c2"))
        })
    }
}