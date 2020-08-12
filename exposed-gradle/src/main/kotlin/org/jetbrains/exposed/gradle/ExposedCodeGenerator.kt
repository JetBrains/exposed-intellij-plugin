package org.jetbrains.exposed.gradle

import com.sksamuel.hoplite.ConfigLoader
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.exposed.gradle.builders.TableBuilder
import org.jetbrains.exposed.gradle.builders.TableBuilderData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.io.File
import org.jetbrains.exposed.sql.Table as ExposedTable


// TODO support schemas
/**
 * Generates files containing Exposed code for given [tables] using config file [configFileName] for a given DB [dialect].
 */
class ExposedCodeGenerator(
        private val tables: List<Table>,
        private val configFileName: String? = null,
        private val dialect: DBDialect? = null
) {
    private val columnToPropertySpec = mutableMapOf<Column, PropertySpec>()
    private val columnToTableSpec = mutableMapOf<Column, TypeSpec>()

    private val columnNameToInitializerBlock = mutableMapOf<String, String>()


    // returns a TypeSpec used for Exposed Kotlin code generation
    private fun generateExposedTable(
            table: Table,
            configuration: ExposedCodeGeneratorConfiguration = ExposedCodeGeneratorConfiguration()
    ): TypeSpec {
        val builder = TableBuilder(table,
                TableBuilderData(columnToPropertySpec, columnToTableSpec, columnNameToInitializerBlock, dialect, configuration)
        )

        builder.generateExposedTableDeclaration()
        builder.generateExposedTableColumns()
        builder.generateExposedTablePrimaryKey()
        builder.generateExposedTableMulticolumnIndexes()

        return builder.build()
    }

    /**
     * Generates file specs for [tables] using [configFileName] as a configuration file and minding [dialect] DB dialect.
     */
    fun generateExposedTables(): List<FileSpec> {
        val config = if (configFileName != null) {
            ConfigLoader().loadConfigOrThrow(files = listOf(File(configFileName)))
        } else {
            ExposedCodeGeneratorConfiguration()
        }

        if (config.columnMappings.isNotEmpty()) {
            columnNameToInitializerBlock.putAll(config.columnMappings)
        }

        return if (config.generateSingleFile) {
            val fileSpec = FileSpec.builder(
                    config.packageName,
                    if (config.generatedFileName.isNullOrBlank()) {
                        defaultFilename
                    } else {
                        config.generatedFileName
                    }
            )
            tables.forEach { fileSpec.addType(generateExposedTable(it, config)) }
            listOf(fileSpec.build())
        } else {
            val fileSpecs = mutableListOf<FileSpec>()
            for (table in tables) {
                val fileSpec = FileSpec.builder(config.packageName, "${table.fullName.toCamelCase(capitalizeFirst = true)}.kt")
                fileSpec.addType(generateExposedTable(table, config))
                fileSpecs.add(fileSpec.build())
            }

            fileSpecs
        }
    }

    companion object {
        val exposedPackage: Package = ExposedTable::class.java.`package`
        private const val defaultFilename = "GeneratedTables.kt"
    }
}

val logger: Logger = LoggerFactory.getLogger("ExposedCodeGeneratorLogger")