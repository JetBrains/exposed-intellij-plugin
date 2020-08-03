package org.jetbrains.exposed.gradle

import com.sksamuel.hoplite.ConfigLoader
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.exposed.gradle.builders.TableBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.io.File
import org.jetbrains.exposed.sql.Table as ExposedTable


// TODO support schemas
class ExposedCodeGenerator(private val tables: List<Table>, private val configFileName: String? = null) {
    private val columnToPropertySpec = mutableMapOf<Column, PropertySpec>()
    private val columnToTableSpec = mutableMapOf<Column, TypeSpec>()

    private val columnNameToInitializerBlock = mutableMapOf<String, String>()


    // returns a TypeSpec used for Exposed Kotlin code generation
    private fun generateExposedTable(table: Table): TypeSpec {
        val builder = TableBuilder(table, columnToPropertySpec, columnToTableSpec, columnNameToInitializerBlock)

        builder.generateExposedTableDeclaration()
        builder.generateExposedTableColumns()
        builder.generateExposedTablePrimaryKey()

        return builder.build()
    }

    fun generateExposedTables(databaseName: String): List<FileSpec> {
        val config = if (configFileName != null) {
            ConfigLoader().loadConfigOrThrow(files = listOf(File(configFileName)))
        } else {
            ExposedCodeGeneratorConfiguration(generatedFileName = "$databaseName.kt")
        }

        if (config.columnMappings.isNotEmpty()) {
            columnNameToInitializerBlock.putAll(config.columnMappings)
        }

        return if (config.generateSingleFile) {
            val fileSpec = FileSpec.builder(
                    config.packageName,
                    if (config.generatedFileName.isNullOrBlank()) {
                        "${databaseName.toCamelCase(capitalizeFirst = true)}.kt"
                    } else {
                        config.generatedFileName
                    }
            )
            tables.forEach { fileSpec.addType(generateExposedTable(it)) }

            listOf(fileSpec.build())
        } else {
            val fileSpecs = mutableListOf<FileSpec>()
            for (table in tables) {
                val fileSpec = FileSpec.builder(config.packageName, "${table.fullName.toCamelCase(capitalizeFirst = true)}.kt")
                fileSpec.addType(generateExposedTable(table))
            }

            fileSpecs
        }
    }

    companion object {
        val exposedPackage: Package = ExposedTable::class.java.`package`
    }
}

val logger: Logger = LoggerFactory.getLogger("ExposedCodeGeneratorLogger")