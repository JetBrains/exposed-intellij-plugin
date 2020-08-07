package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateTimeColumnType
import org.jetbrains.exposed.gradle.databases.*
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import org.junit.Test
import java.nio.file.Paths


class ExposedCodeGeneratorTest : ExposedCodeGeneratorFromTablesTest() {
    @Test
    fun charTypes() {
        testByCompilation(listOf(CharTypes), {
            with(TableChecker("CharTypes")) {
                checkTableObject("char_types", {
                    checkColumnProperty("charColumn", "char_column", CharColumnType(5))
                    checkColumnProperty("varcharColumn", "varchar_column", VarCharColumnType(5))
                    checkColumnProperty("textColumn", "text_column", TextColumnType())
                })
            }
        })
    }

    @Test
    // PostgreSQL doesn't allow for TINYINT columns
    fun integerTypes() {
        testByCompilation(listOf(IntegerTypes), {
            with(TableChecker("IntegerTypes")) {
                checkTableObject("integer_types", {
                    checkColumnProperty("tinyIntColumn", "tiny_int_column", ByteColumnType())
                    checkColumnProperty("shortColumn", "short_column", ShortColumnType())
                    checkColumnProperty("integerColumn", "integer_column", IntegerColumnType())
                    checkColumnProperty("longColumn", "long_column", LongColumnType())
                })
            }

        }, excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgreSQL uses INT2 in place of TINYINT
    fun integerTypesPostgres() {
        testByCompilation(listOf(IntegerTypes), {
            with(TableChecker("IntegerTypes")) {
                checkTableObject("integer_types", {
                    checkColumnProperty("tinyIntColumn", "tiny_int_column", ShortColumnType())
                    checkColumnProperty("shortColumn", "short_column", ShortColumnType())
                    checkColumnProperty("integerColumn", "integer_column", IntegerColumnType())
                    checkColumnProperty("longColumn", "long_column", LongColumnType())
                })
            }
        }, excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgreSQL erases binary columns length
    fun miscTypes() {
        testByCompilation(listOf(MiscTypes), {
            with(TableChecker("MiscTypes")) {
                checkTableObject("misc_types", {
                    checkColumnProperty("booleanColumn", "boolean_column", BooleanColumnType())
                    checkColumnProperty("binaryColumn", "binary_column", BinaryColumnType(32))
                    checkColumnProperty("blobColumn", "blob_column", BlobColumnType())
                })
            }
        }, excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    // PostgresSQL: BLOB -> Binary, binary length is always the maximal possible
    fun miscTypesPostgres() {
        testByCompilation(listOf(MiscTypes), {
            with(TableChecker("MiscTypes")) {
                checkTableObject("misc_types", {
                    checkColumnProperty("booleanColumn", "boolean_column", BooleanColumnType())
                    checkColumnProperty("binaryColumn", "binary_column", BinaryColumnType(2147483647))
                    checkColumnProperty("blobColumn", "blob_column", BinaryColumnType(2147483647))
                })
            }
        }, excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    fun decimalTypes() {
        testByCompilation(listOf(DecimalTypes), {
            with(TableChecker("DecimalTypes")) {
                checkTableObject("decimal_types", {
                    checkColumnProperty("decimalColumn1", "decimal_column_1", DecimalColumnType(10, 0))
                    checkColumnProperty("decimalColumn2", "decimal_column_2", DecimalColumnType(10, 2))
                })
            }
        })
    }

    @Test
    // SQLite stores LocalDateTime as Numeric, making it indistinguishable from actual numeric/decimal columns
    fun dateTimeTypes() {
        testByCompilation(listOf(DateTimeTypes), {
            with(TableChecker("DateTimeTypes")) {
                checkTableObject("date_time_types", {
                    checkColumnProperty("dateColumn", "date_column", JavaLocalDateColumnType())
                    checkColumnProperty("dateTimeColumn", "date_time_column", JavaLocalDateTimeColumnType())
                })
            }
        }, excludedDbList = listOf(TestDB.SQLITE))
    }

    @Test
    fun nullableTypes() {
        testByCompilation(listOf(NullableTypes), {
            with(TableChecker("NullableTypes")) {
                checkTableObject("nullable_types",  {
                    checkColumnProperty("c1", "c1", IntegerColumnType().apply { nullable = true }, isNullable = true)
                    checkColumnProperty("c2", "c2", LongColumnType().apply { nullable = true }, isNullable = true)
                    checkColumnProperty("c3", "c3", DoubleColumnType().apply { nullable = true }, isNullable = true)
                    checkColumnProperty("c4", "c4", CharColumnType(5).apply { nullable = true }, isNullable = true)
                    checkColumnProperty("c5", "c5", BooleanColumnType().apply { nullable = true }, isNullable = true)
                })
            }
        })
    }

    @Test
    fun selfForeignKey() {
        testByCompilation(listOf(SelfForeignKeyTable), {
            with(TableChecker("SelfForeignKeyTable")) {
                checkTableObject("self_foreign_key_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType(), foreignKeyFrom = "c2", foreignKeyTarget = "c1")
                    checkColumnProperty("c3", "c3", IntegerColumnType())
                }, indexes = listOf(CompilationResultChecker.IndexWrapper(isUnique = true, columnNames = setOf("c1"))))
            }
        })
    }

    @Test
    fun foreignKey() {
        testByCompilation(listOf(Sample, SampleRef), {
            with(TableChecker("Sample")) {
                checkTableObject("sample", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", TextColumnType())
                }, indexes = listOf(CompilationResultChecker.IndexWrapper(isUnique = true, columnNames = setOf("c1"))))
            }
            with(TableChecker("SampleRef")) {
                checkTableObject("sample_ref", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType(),
                            foreignKeyFrom = "c2", foreignKeyTarget = "c1", foreignKeyTargetTable = "Sample")
                })
            }
        })
    }

    @Test
    fun autoIncrement() {
        testByCompilation(listOf(AutoIncrementTable), {
            with(TableChecker("AutoIncrementTable")) {
                checkTableObject("auto_increment_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType(), isAutoIncremented = true)
                    checkColumnProperty("c2", "c2", LongColumnType(), isAutoIncremented = true)
                })
            }
        }, excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL))
    }

    @Test
    fun idTables() {
        testByCompilation(listOf(Sample1, Sample2, Sample3, Sample4), {
            with(TableChecker("Sample1")) {
                checkTableObject("sample_1", {
                    checkColumnProperty("str", "str", TextColumnType())
                    checkColumnProperty("id", "id", IntegerColumnType(), isAutoIncremented = true, isEntityId = true)
                }, tableClass = IntIdTable::class, primaryKeyColumns = listOf("id"))
            }
            with(TableChecker("Sample2")) {
                checkTableObject("sample_2", {
                    checkColumnProperty("str", "str", TextColumnType())
                    checkColumnProperty("id", "id", LongColumnType(), isAutoIncremented = true, isEntityId = true)
                }, tableClass = LongIdTable::class, primaryKeyColumns = listOf("id"))
            }
            with(TableChecker("Sample3")) {
                checkTableObject("sample_3", {
                    checkColumnProperty("str", "str", TextColumnType())
                    checkColumnProperty("id", "id", UUIDColumnType(), isEntityId = true)
                }, tableClass = UUIDTable::class, primaryKeyColumns = listOf("id"))
            }
            with(TableChecker("Sample4")) {
                checkTableObject("sample_4", {
                    checkColumnProperty("str", "str", TextColumnType())
                    checkColumnProperty("id", "id", VarCharColumnType(30), isEntityId = true)
                }, tableClass = IdTable::class, primaryKeyColumns = listOf("id"))
            }

        }, excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL))
    }

    @Test
    fun primaryKey() {
        testByCompilation(listOf(SinglePrimaryKeyTable, CompositePrimaryKeyTable), {
            with(TableChecker("SinglePrimaryKeyTable")) {
                checkTableObject("single_primary_key_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType())
                }, primaryKeyColumns = listOf("c1"))
            }
            with(TableChecker("CompositePrimaryKeyTable")) {
                checkTableObject("composite_primary_key_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType())
                    checkColumnProperty("c3", "c3", IntegerColumnType())
                }, primaryKeyColumns = listOf("c1", "c2"))
            }
        })
    }

    @Test
    fun mappedColumn() {
        testByCompilation(listOf(MappedColumnTable), {
            with(TableChecker("MappedColumnTable", "org.jetbrains.exposed.gradle.test")) {
                checkTableObject("mapped_column_table", {
                    checkColumnProperty("floatColumn", "float_column", FloatColumnType())
                    checkColumnProperty("integerColumn", "integer_column", IntegerColumnType())
                })
            }
        }, configFileName = Paths.get(resourcesConfigFilesPath.toString(), "floatColumnMappedConfig.yml").toString())
    }

    @Test
    fun hashIndexPostgreSQL() {
        testByCompilation(listOf(HashIndexTable), {
            with(TableChecker("HashIndexTable")) {
                checkTableObject("hash_index_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", TextColumnType())
                }, indexes = listOf(
                        CompilationResultChecker.IndexWrapper("custom_index_name", false, setOf("c1"), "HASH")
                ))
            }
        }, excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG))
    }

    @Test
    fun multiColumnIndex() {
        testByCompilation(listOf(MultiColumnIndexTable), {
            with(TableChecker("MultiColumnIndexTable")) {
                checkTableObject("multi_column_index_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", TextColumnType())
                    checkColumnProperty("c3", "c3", IntegerColumnType())
                }, indexes = listOf(
                        CompilationResultChecker.IndexWrapper("custom_index_name", false, setOf("c1", "c3")),
                        CompilationResultChecker.IndexWrapper("custom_unique_index_name", true, setOf("c3"))
                ))
            }
        }, excludedDbList = listOf(TestDB.H2, TestDB.H2_MYSQL))
    }

    @Test
    fun indexes() {
        testByCompilation(listOf(IndexTable), {
            with(TableChecker("IndexTable")) {
                checkTableObject("index_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType())
                }, indexes = listOf(
                        CompilationResultChecker.IndexWrapper("idx1", false, setOf("c1")),
                        CompilationResultChecker.IndexWrapper("idx2", false, setOf("c2"))
                ))
            }
        })
    }

    @Test
    fun uniqueIndexes() {
        testByCompilation(listOf(UniqueIndexTable), {
            with(TableChecker("UniqueIndexTable")) {
                checkTableObject("unique_index_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType())
                }, indexes = listOf(
                        CompilationResultChecker.IndexWrapper("idx1", true, setOf("c2")),
                        CompilationResultChecker.IndexWrapper("idx2", true, setOf("c1", "c2"))
                ))
            }
        }, excludedDbList = listOf(TestDB.H2, TestDB.H2_MYSQL))
    }

    @Test
    fun unnamedIndexes() {
        testByCompilation(listOf(UnnamedIndexTable), {
            with(TableChecker("UnnamedIndexTable")) {
                checkTableObject("unnamed_index_table", {
                    checkColumnProperty("c1", "c1", IntegerColumnType())
                    checkColumnProperty("c2", "c2", IntegerColumnType())
                }, indexes = listOf(
                        CompilationResultChecker.IndexWrapper("unnamed_index_table_c2", false, setOf("c2")),
                        CompilationResultChecker.IndexWrapper("unnamed_index_table_c1_c2", false, setOf("c1", "c2"))
                ))
            }
        })
    }
}