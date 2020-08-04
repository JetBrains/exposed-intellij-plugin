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

open class ExposedCodeGeneratorCompilationTest : DatabaseTestsBase() {
    class CompilationResultChecker(private val result: KotlinCompilation.Result) {
        inner class TableChecker(tablePropertyName: String, tablePackageName: String = "") {
            private val packagePrefix = formPackageName(tablePackageName)
            private val tableObjectClass = result.classLoader.loadClass("$packagePrefix$tablePropertyName").kotlin
            private val tableObjectInstance = tableObjectClass.objectInstance!!

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
                        result.classLoader.loadClass("${formPackageName(tableObjectInstance::class.java.packageName)}$foreignKeyTargetTable").kotlin.objectInstance
                    }!!
                    val target = foreignKeyTargetTableObject::class.memberProperties
                            .find { it.name == foreignKey.targetColumn.toLowerCase() }!!.getter.call(foreignKeyTargetTableObject)
                    assertThat(foreignKey.target).isEqualTo(target)
                } else {
                    assertThat(columnValue.foreignKey).isNull()
                }
            }

            fun checkTableObject(
                    tableName: String,
                    checkPropertiesBlock: () -> Unit,
                    tableClass: KClass<*> = Table::class,
                    primaryKeyColumns: List<String> = emptyList()
            ) {
                assertThat(tableObjectClass.supertypes).hasSize(1)
                assertThat(tableObjectClass.supertypes[0].classifier).isEqualTo(tableClass)
                assertThat((tableObjectInstance as Table).tableName).isEqualTo(tableName)

                checkPropertiesBlock()

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

            private fun formPackageName(packageName: String) = if (packageName.isNotBlank()) "$packageName." else ""
        }
    }

    protected fun compileExposedFile(fileSpec: FileSpec): KotlinCompilation.Result {
        val sb = StringBuilder()
        fileSpec.writeTo(sb)
        val kotlinSource = SourceFile.kotlin(fileSpec.name, sb.toString())
        return KotlinCompilation().apply {
            sources = listOf(kotlinSource)

            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
    }


}

open class ExposedCodeGeneratorFromTablesTest : ExposedCodeGeneratorCompilationTest() {
    fun testByCompilation(
            tables: List<Table>,
            checkTablesBlock: CompilationResultChecker.() -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null,
            configFileName: String? = null
    ) {
        withTables(excludeSettings = excludedDbList, tables = *tables.toTypedArray(), statement = {
            // TODO adapt for multiple file specs
            val fileSpec = getDatabaseExposedFileSpec(it, tableName, configFileName)[0]
            val result = compileExposedFile(fileSpec)

            // if it didn't compile then there might be imports missing, incorrect types, etc
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlock(CompilationResultChecker(result))
        })
    }
}

open class ExposedCodeGeneratorFromScriptTest : ExposedCodeGeneratorCompilationTest() {
    private fun getSQLScriptCommands(script: String): List<String> {
        val splitResults = script.split(Regex("((?<=INSERT)|(?=INSERT))|((?<=CREATE)|(?=CREATE))|((?<=DROP)|(?=DROP))|((?<=--)|(?=--))")).filterNot { it.isBlank() }
        val commands = mutableListOf<String>()
        for (i in splitResults.indices step 2) {
            commands.add("${splitResults[i]} ${splitResults[i + 1]}")
        }
        return commands
    }

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
                        checkTableObject(tableName, { checkColumnsBlock(this) })
                    }
                },
                TestDB.enabledInTests() - db,
                tableName
        )
    }
}