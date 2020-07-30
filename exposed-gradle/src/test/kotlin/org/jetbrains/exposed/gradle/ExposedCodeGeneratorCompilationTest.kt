package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.*
import java.lang.StringBuilder
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

open class ExposedCodeGeneratorCompilationTest : DatabaseTestsBase() {
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

    protected fun checkColumnProperty(
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

    protected fun checkTableObject(
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
}

open class ExposedCodeGeneratorFromTablesTest : ExposedCodeGeneratorCompilationTest() {
    fun testByCompilation(
            tables: List<Table>,
            checkTablesBlock: (KotlinCompilation.Result) -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null
    ) {
        withTables(excludeSettings = excludedDbList, tables = *tables.toTypedArray(), statement = {
            val fileSpec = getDatabaseExposedFileSpec(it, tableName)
            val result = compileExposedFile(fileSpec)

            // if it didn't compile then there might be imports missing, incorrect types, etc
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlock(result)
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
            checkTablesBlock: (KotlinCompilation.Result) -> Unit,
            excludedDbList: List<TestDB> = emptyList(),
            tableName: String? = null
    ) {
        val scriptFile = Paths.get(scriptFilePath.toString(), scriptFileName).toFile()
        val script = scriptFile.readText()
        val commands = getSQLScriptCommands(script)
        withDb(excludeSettings = excludedDbList, statement = {
            // todo tokenize
            commands.forEach { command -> exec(command) }
            commit()

            val fileSpec = getDatabaseExposedFileSpec(it, tableName)
            val result = compileExposedFile(fileSpec)

            // if it didn't compile then there might be imports missing, incorrect types, etc
            assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
            checkTablesBlock(result)
        })
    }
}