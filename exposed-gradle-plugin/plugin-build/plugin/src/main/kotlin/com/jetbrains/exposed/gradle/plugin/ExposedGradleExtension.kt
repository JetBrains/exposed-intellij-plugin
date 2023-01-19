package com.jetbrains.exposed.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

const val DEFAULT_OUTPUT_DIRECTORY = "tables"

@Suppress("UnnecessaryAbstractClass")
abstract class ExposedGradleExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    var propertiesFilename: String? = null

    var databaseDriver: String? = null
    var databaseName: String? = null
    var user: String? = null
    var password: String? = null
    var host: String? = null
    var port: String? = null
    var ipv6Host: String? = null
    var connectionProperties: Map<String, String> = mutableMapOf()


    var connectionURL: String? = null

    var packageName: String? = null
//    var generateSingleFile: Boolean = true
    var generatedFileName: String? = null
    var collate: String? = null
    var columnMappings: Map<String, String> = mutableMapOf()

    var configFilename: String? = null

    var outputDirectory: DirectoryProperty = objects.directoryProperty().convention(
            project.layout.buildDirectory.dir(DEFAULT_OUTPUT_DIRECTORY)
    )

    var dateTimeProvider: String? = null
}
