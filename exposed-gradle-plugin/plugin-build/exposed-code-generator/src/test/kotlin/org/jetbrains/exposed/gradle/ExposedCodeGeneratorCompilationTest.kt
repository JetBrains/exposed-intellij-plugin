package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Tests tables compiled to Exposed code in [result].
 */
class CompilationResultChecker(private val result: KotlinCompilation.Result) {
    inner class TableChecker(tablePropertyName: String, tablePackageName: String = "") {
        private val packagePrefix = formPackageName(tablePackageName)
        private val tableObjectClass = result.classLoader.loadClass("$packagePrefix$tablePropertyName").kotlin
        private val tableObjectInstance = tableObjectClass.objectInstance!!

        /**
         * Checks that [tableObjectClass] has a column with [columnPropertyName] of type [columnType] and its constraints.
         */
        fun checkColumnProperty(
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
            val property = tableObjectInstance::class.memberProperties.find { it.name == columnPropertyName } ?: fail("Property $columnPropertyName not found.")

            assertThat(property.returnType.classifier).isEqualTo(Column::class)

            val columnValue = property.getter.call(tableObjectInstance)
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
                val foreignKeyTargetTableObject = if (foreignKeyTargetTable == null || foreignKeyTargetTable == tableObjectInstance::class.simpleName) {
                    tableObjectInstance
                } else {
                    val packageName = tableObjectInstance::class.java.`package`?.name ?: ""
                    result.classLoader.loadClass("${formPackageName(packageName)}$foreignKeyTargetTable").kotlin.objectInstance
                }!!
                val target = foreignKeyTargetTableObject::class.memberProperties
                        .find { it.name == foreignKey.targetColumn.toLowerCase() }!!.getter.call(foreignKeyTargetTableObject)
                assertThat(foreignKey.target).isEqualTo(target)
            } else {
                assertThat(columnValue.foreignKey).isNull()
            }
        }

        /**
         * Checks [tableName] table definition for its superclass [tableClass],
         * attributes such as [primaryKeyColumns], and checks table columns using [checkColumnsBlock].
         */
        fun checkTableObject(
                tableName: String,
                checkColumnsBlock: () -> Unit,
                tableClass: KClass<*> = Table::class,
                primaryKeyColumns: List<String> = emptyList(),
                indexes: List<IndexWrapper> = emptyList()
        ) {
            // check table object
            assertThat(tableObjectClass.supertypes).hasSize(1)
            assertThat(tableObjectClass.supertypes[0].classifier).isEqualTo(tableClass)
            assertThat((tableObjectInstance as Table).tableName).isEqualTo(tableName)

            // check columns
            checkColumnsBlock()

            // check primary key
            if (primaryKeyColumns.isNotEmpty()) {
                assertThat(tableObjectInstance.primaryKey).isNotNull
                assertThat(tableObjectInstance.primaryKey!!.columns).hasSameSizeAs(primaryKeyColumns)
                primaryKeyColumns.forEach { column ->
                    assertThat(tableObjectInstance.primaryKey!!.columns).anyMatch { it.name == column }
                }
            } else {
                assertThat(tableObjectInstance.primaryKey).isNull()
            }

            // check indexes
            assertThat(tableObjectInstance.indices).hasSameSizeAs(indexes)
            for (index in indexes) {
                val tableIndex = if (index.name != null) {
                    tableObjectInstance.indices.find { it.indexName == index.name }
                } else {
                    // when the name is irrelevant
                    tableObjectInstance.indices.find { it.unique == index.isUnique && it.columns.map { it.name }.toSet() == index.columnNames }
                }
                assertThat(tableIndex).isNotNull
                assertThat(tableIndex!!.unique).isEqualTo(index.isUnique)
                assertThat(index.columnNames).isEqualTo(tableIndex.columns.map { it.name }.toSet())
                assertThat(tableIndex.indexType).isEqualTo(index.type)
            }
        }

        private fun formPackageName(packageName: String) = if (packageName.isNotBlank()) "$packageName." else ""
    }

    data class IndexWrapper(
            val name: String? = null,
            val isUnique: Boolean = false,
            val columnNames: Set<String> = emptySet(),
            val type: String? = null
    )
}

/**
 * Test Exposed code generation by compiling file specs.
 */
open class ExposedCodeGeneratorCompilationTest : DatabaseTestsBase() {
    protected fun compileExposedFile(vararg fileSpecs: FileSpec): KotlinCompilation.Result {
        val kotlinSources = mutableListOf<SourceFile>()
        for (fileSpec in fileSpecs) {
            val sb = StringBuilder()
            fileSpec.writeTo(sb)
            kotlinSources.add(SourceFile.kotlin("${fileSpec.name}.kt", sb.toString()))
        }

        return KotlinCompilation().apply {
            sources = kotlinSources
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }
}

/**
 * Tests Exposed code generation by creating tables using Exposed, reading back their metadata,
 * generating Exposed tables based on that metadata, compiling these tables and checking with [CompilationResultChecker].
 */
open class ExposedCodeGeneratorFromTablesTest : ExposedCodeGeneratorCompilationTest() {
    /**
     * Creates [tables] using Exposed, reads their metadata, generates Exposed tables from it,
     * compiles those tables and checks them with [checkTablesBlocks].
     * Run on all DB dialects from [TestDB.enabledInTests] except [excludedDbList].
     * [configFileName] provides a config file for code generation.
     */
    fun testByCompilation(
            tables: List<Table>,
            vararg checkTablesBlocks: CompilationResultChecker.() -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null,
            configFileName: String? = null
    ) {
        withTables(excludeSettings = excludedDbList, tables = *tables.toTypedArray(), statement = {
            val fileSpecs = getDatabaseExposedFileSpec(it, tableName, configFileName)
            val result = compileExposedFile(*fileSpecs.toTypedArray())
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlocks.forEach { it(CompilationResultChecker(result)) }
        })
    }

    /**
     * Checks a singular Exposed [table] code generation.
     */
    fun testTableByCompilation(
            table: Table,
            checkColumnsBlock: CompilationResultChecker.TableChecker.() -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            configFileName: String? = null,
            tableClass: KClass<*> = Table::class,
            primaryKeyColumns: List<String> = emptyList(),
            indexes: List<CompilationResultChecker.IndexWrapper> = emptyList()
    ) {
        testByCompilation(listOf(table), {
            with(TableChecker(table::class.simpleName!!)) {
                checkTableObject(table.tableName, { checkColumnsBlock() }, tableClass, primaryKeyColumns, indexes)
            }
        }, excludedDbList = excludedDbList, configFileName = configFileName)
    }
}

/**
 * Tests Exposed code generation by running SQL scripts using Exposed, reading table metadata,
 * generating Exposed tables from it, compiling tables and checking these tables with [CompilationResultChecker].
 */
open class ExposedCodeGeneratorFromScriptTest : ExposedCodeGeneratorCompilationTest() {
    private fun getSQLScriptCommands(script: String): List<String> {
        val splitResults = script.split(Regex("((?<=INSERT)|(?=INSERT))|((?<=CREATE)|(?=CREATE))|((?<=DROP)|(?=DROP))|((?<=--)|(?=--))")).filterNot { it.isBlank() }
        val commands = mutableListOf<String>()
        for (i in splitResults.indices step 2) {
            commands.add("${splitResults[i]} ${splitResults[i + 1]}")
        }
        return commands
    }

    /**
     * Uses Exposed to create tables as specified in [scriptFileName] from [scriptFilePath],
     * reads their metadata, generates Exposed tables from it,
     * compiles those tables and checks them with [checkTablesBlock].
     * Run on all DB dialects from [TestDB.enabledInTests] except [excludedDbList].
     */
    fun testByCompilation(
            scriptFileName: String,
            scriptFilePath: Path,
            checkTablesBlock: CompilationResultChecker.() -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null
    ) {
        val scriptFile = Paths.get(scriptFilePath.toString(), scriptFileName).toFile()
        val script = scriptFile.readText()
        val commands = getSQLScriptCommands(script)
        withDb(excludeSettings = excludedDbList, statement = {
            commands.forEach { command -> exec(command) }
            commit()

            // TODO adapt for multiple file specs
            val fileSpec = getDatabaseExposedFileSpec(it, tableName)[0]
            val result = compileExposedFile(fileSpec)

            // if it didn't compile then there might be imports missing, incorrect types, etc
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlock(CompilationResultChecker(result))
        })
    }
}

// used for testing specific DB dialects on resource files for those specific DBs
/**
 * Test Exposed code generation for a specific database created from file [dbFileName] in [dbDirectoryName] in resources
 * for databases listed in [db] using compilation.
 */
open class ExposedCodeGeneratorDBTest(
        private val dbFileName: String,
        private val dbDirectoryName: String,
        private val db: List<TestDB>
) : ExposedCodeGeneratorFromScriptTest() {
    protected fun runTableTest(tableName: String, checkTablesBlock: CompilationResultChecker.() -> Unit) {
        testByCompilation(
                dbFileName,
                Paths.get(resourcesDatabasesPath.toString(), dbDirectoryName),
                checkTablesBlock,
                TestDB.enabledInTests() - db,
                tableName
        )
    }

    protected fun runTableTest(
            tableName: String,
            tableObjectName: String,
            checkColumnsBlock: CompilationResultChecker.TableChecker.() -> Unit
    ) {
        testByCompilation(
                dbFileName,
                Paths.get(resourcesDatabasesPath.toString(), dbDirectoryName),
                {
                    with(TableChecker(tableObjectName)) {
                        checkTableObject(tableName, { checkColumnsBlock() })
                    }
                },
                TestDB.enabledInTests() - db,
                tableName
        )
    }
}