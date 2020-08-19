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
            it.databaseDriver.set(getPropertyValue<String>(project, extension, "databaseDriver"))
            it.databaseName.set(getPropertyValue<String>(project, extension, "databaseName"))
            it.user.set(getPropertyValue<String>(project, extension, "user"))
            it.password.set(getPropertyValue<String>(project, extension, "password"))
            it.host.set(getPropertyValue<String>(project, extension, "host"))
            it.port.set(getPropertyValue<String>(project, extension, "port"))
            it.ipv6Host.set(getPropertyValue<String>(project, extension, "ipv6Host"))

            it.connectionURL.set(getPropertyValue<String>(project, extension, "connectionURL"))

            it.packageName.set(getPropertyValue<String>(project, extension, "packageName"))
            it.generateSingleFile.set(getPropertyValue<Boolean>(project, extension, "generateSingleFile"))
            it.generatedFileName.set(getPropertyValue<String>(project, extension, "generatedFileName"))
            it.collate.set(getPropertyValue<String>(project, extension, "collate"))
            // TODO
            it.columnMappings.set(extension.columnMappings)

            it.outputDirectory.set(extension.outputDirectory)
        }
    }

    // IMPORTANT: the property should have the same name everywhere bc of reflection usage
    @Suppress("UNCHECKED_CAST")
    private fun <T> getPropertyValue(project: Project, extension: ExposedGradleExtension, propName: String): T? = when {
        project.hasProperty(propName) -> project.property(propName) as T
        extension.propertiesFilename.orNull != null -> {
            val props = Properties()
            props.load(FileReader(extension.propertiesFilename.get()))
            props[propName] as T?
        }
        else -> {
            val prop = extension::class.declaredMemberProperties.find { it.name == propName }
            val value = prop!!.getter.call(extension) as Property<T>
            value.orNull
        }
    }
}
