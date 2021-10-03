package com.jetbrains.exposed.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jetbrains.exposed.gradle.ExposedCodeGenerator
import org.jetbrains.exposed.gradle.ExposedCodeGeneratorConfiguration
import org.jetbrains.exposed.gradle.MetadataGetter


abstract class ExposedGenerateCodeTask : DefaultTask() {

    init {
        description = "Generate Exposed table code for DB"
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    @get:Option(option = "databaseDriver", description = "Which database to connect to, in form of JDBC driver string, such as jdbc:sqlite")
    @get:Optional
    abstract val databaseDriver: Property<String>

    @get:Input
    @get:Option(option = "databaseName", description = "The name of the database to connect to")
    @get:Optional
    abstract val databaseName: Property<String>

    @get:Input
    @get:Option(option = "user", description = "Database user name")
    @get:Optional
    abstract val user: Property<String>

    @get:Input
    @get:Option(option = "pass", description = "Database password")
    @get:Optional
    abstract val password: Property<String>

    @get:Input
    @get:Option(option = "host", description = "Database host using IPv4")
    @get:Optional
    abstract val host: Property<String>

    @get:Input
    @get:Option(option = "port", description = "Database port")
    @get:Optional
    abstract val port: Property<String>

    @get:Input
    @get:Option(option = "ipv6Host", description = "Database host using IPv6; use either this or host")
    @get:Optional
    abstract val ipv6Host: Property<String>

    @get:Input
    @get:Option(option = "connectionURL", description = "full connection URL")
    @get:Optional
    abstract val connectionURL: Property<String>

    @get:Input
    @get:Option(option = "connectionProperties", description = "Additional connection properties. Will be added to jdbc connection")
    @get:Optional
    abstract val connectionProperties: MapProperty<String, String>

    @get:Input
    @get:Option(option = "packageName", description = "Generated files will be placed in this package")
    @get:Optional
    abstract val packageName: Property<String>

    @get:Input
    @get:Option(
        option = "generateSingleFile",
        description = "Set to true for generating all tables in one file; a separate file for each table is generated otherwise"
    )
    @get:Optional
    abstract val generateSingleFile: Property<Boolean>

    @get:Input
    @get:Option(
        option = "generatedFileName",
        description = "If generatedSingleFile is set to true, this will be the name of the file generated"
    )
    @get:Optional
    abstract val generatedFileName: Property<String>

    @get:Input
    @get:Option(option = "collate", description = "String collation method for all string columns in DB")
    @get:Optional
    abstract val collate: Property<String>

    @get:Input
    @get:Option(
        option = "columnMappings",
        description = "Set column mappings manually, in the form of [tableName].[columnName] = [exposed function call], " +
                "e.g. testTable.floatColumn = float(\"float_column\")"
    )
    @get:Optional
    abstract val columnMappings: MapProperty<String, String>

    @get:Input
    @get:Option(option = "configFilename", description = "Config filename")
    @get:Optional
    abstract val configFilename: Property<String>


    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generateExposedCode() {
        val metadataGetter = if (connectionURL.orNull != null) {
            MetadataGetter({ connectionURL.get() }, user.orNull, password.orNull)
        } else {
            MetadataGetter(
                    databaseDriver.get(),
                    databaseName.get(),
                    user.orNull,
                    password.orNull,
                    host.orNull,
                    port.orNull,
                    ipv6Host.orNull,
                    connectionProperties.orNull
            )
        }


        val tables = metadataGetter.getTables().filterUtilTables()
        val exposedCodeGenerator = if (configFilename.orNull != null) {
            ExposedCodeGenerator(tables, configFilename.get())
        } else {
            val config = ExposedCodeGeneratorConfiguration(
                    packageName.getOrElse(""),
                    // TODO
                    true,
                    generatedFileName.orNull,
                    collate.orNull,
                    columnMappings.getOrElse(emptyMap())
            )
            ExposedCodeGenerator(tables, config)
        }
        val files = exposedCodeGenerator.generateExposedTables()

        files.forEach {
            val directory = outputDirectory.get()
            it.writeTo(directory.asFile)
            val generatedFile = directory.file(it.toJavaFileObject().name).asFile
            val generatedContent = generatedFile.readText()
            generatedFile.writeText(ExposedCodeGenerator.postProcessOutput(generatedContent))
        }
    }

    private fun List<schemacrawler.schema.Table>.filterUtilTables() = this.filterNot { it.fullName.startsWith("sys.") }
}
