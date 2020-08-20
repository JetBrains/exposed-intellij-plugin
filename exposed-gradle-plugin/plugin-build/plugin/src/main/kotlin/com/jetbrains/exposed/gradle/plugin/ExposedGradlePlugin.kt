package com.jetbrains.exposed.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.FileReader
import java.util.*
import kotlin.reflect.full.declaredMemberProperties

const val EXTENSION_NAME = "exposedCodeGeneratorConfig"
const val TASK_NAME = "generateExposedCode"

abstract class ExposedGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, ExposedGradleExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, ExposedGenerateCodeTask::class.java) {
            it.databaseDriver.set(getStringProperty(project, extension, "databaseDriver"))
            it.databaseName.set(getStringProperty(project, extension, "databaseName"))
            it.user.set(getStringProperty(project, extension, "user"))
            it.password.set(getStringProperty(project, extension, "password"))
            it.host.set(getStringProperty(project, extension, "host"))
            it.port.set(getStringProperty(project, extension, "port"))
            it.ipv6Host.set(getStringProperty(project, extension, "ipv6Host"))

            it.connectionURL.set(getStringProperty(project, extension, "connectionURL"))

            it.packageName.set(getStringProperty(project, extension, "packageName"))
            it.generateSingleFile.set(getStringProperty(project, extension, "generateSingleFile")?.toBoolean())
            it.generatedFileName.set(getStringProperty(project, extension, "generatedFileName"))
            it.collate.set(getStringProperty(project, extension, "collate"))
            // TODO
            it.columnMappings.set(extension.columnMappings)

            it.outputDirectory.set(extension.outputDirectory)
        }
    }

    // IMPORTANT: the property should have the same name everywhere bc of reflection usage
    @Suppress("UNCHECKED_CAST")
    private fun getProperty(project: Project, extension: ExposedGradleExtension, propName: String): Any? = when {
        System.getProperty(propName) != null -> System.getProperty(propName)
        System.getenv(propName) != null -> System.getenv(propName)
        project.hasProperty(propName) -> project.property(propName)
        extension.propertiesFilename.orNull != null -> {
            val props = Properties()
            props.load(FileReader(extension.propertiesFilename.get()))
            props[propName]
        }
        else -> {
            val prop = extension::class.declaredMemberProperties.find { it.name == propName }
            val value = prop!!.getter.call(extension) as Property<Any>
            value.orNull
        }
    }

    private fun getStringProperty(project: Project, extension: ExposedGradleExtension, propName: String) =
            getProperty(project, extension, propName) as String?
}
