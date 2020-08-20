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
 * Generates files containing Exposed code for given [tables] using config [configuration] for a given DB [dialect].
 */
class ExposedCodeGenerator {
    private val tables: List<Table>
    private val dialect: DBDialect?
    private val configuration: ExposedCodeGeneratorConfiguration

    constructor(
            tables: List<Table>,
            config: ExposedCodeGeneratorConfiguration = ExposedCodeGeneratorConfiguration(),
            dialect: DBDialect? = null
    ) {
        this.tables = tables
        this.configuration = config
        this.dialect = dialect
    }

    constructor(tables: List<Table>, configFileName: String, dialect: DBDialect? = null) {
        this.tables = tables
        this.configuration = ConfigLoader().loadConfigOrThrow(files = listOf(File(configFileName)))
        this.dialect = dialect
    }


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
     * Generates file specs for [tables] using [configuration] and minding [dialect] DB dialect.
     */
    fun generateExposedTables(): List<FileSpec> {
        if (configuration.columnMappings.isNotEmpty()) {
            columnNameToInitializerBlock.putAll(configuration.columnMappings)
        }

        return if (configuration.generateSingleFile) {
            val fileSpec = FileSpec.builder(
                    configuration.packageName,
                    if (configuration.generatedFileName.isNullOrBlank()) {
                        defaultFilename
                    } else {
                        configuration.generatedFileName
                    }
            )
            tables.forEach { fileSpec.addType(generateExposedTable(it, configuration)) }
            listOf(fileSpec.build())
        } else {
            val fileSpecs = mutableListOf<FileSpec>()
            for (table in tables) {
                val fileSpec = FileSpec.builder(configuration.packageName, table.fullName.toCamelCase(capitalizeFirst = true))
                fileSpec.addType(generateExposedTable(table, configuration))
                fileSpecs.add(fileSpec.build())
            }

            fileSpecs
        }
    }

    companion object {
        val exposedPackage: Package = ExposedTable::class.java.`package`
        private const val defaultFilename = "GeneratedTables"
    }
}

val logger: Logger = LoggerFactory.getLogger("ExposedCodeGeneratorLogger")