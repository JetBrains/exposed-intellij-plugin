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

    val propertiesFilename: Property<String> = objects.property(String::class.java)

    val databaseDriver: Property<String> = objects.property(String::class.java)
    val databaseName: Property<String> = objects.property(String::class.java)
    val user: Property<String> = objects.property(String::class.java)
    val password: Property<String> = objects.property(String::class.java)
    val host: Property<String> = objects.property(String::class.java)
    val port: Property<String> = objects.property(String::class.java)
    val ipv6Host: Property<String> = objects.property(String::class.java)

    val connectionURL: Property<String> = objects.property(String::class.java)

    val packageName: Property<String> = objects.property(String::class.java)
    val generateSingleFile: Property<Boolean> = objects.property(Boolean::class.java)
    val generatedFileName: Property<String> = objects.property(String::class.java)
    val collate: Property<String> = objects.property(String::class.java)
    val columnMappings: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    val configFilename: Property<String> = objects.property(String::class.java)

    val outputDirectory: DirectoryProperty = objects.directoryProperty().convention(
            project.layout.buildDirectory.dir(DEFAULT_OUTPUT_DIRECTORY)
    )
}
