package org.jetbrains.exposed.gradle

/**
 * User configuration for generating Exposed code.
 */
data class ExposedCodeGeneratorConfiguration(
        val packageName: String = "", // generated files package
        val generateSingleFile: Boolean = true, // all tables are written to a single file if true, each to a separate file otherwise
        val generatedFileName: String? = if (generateSingleFile) "" else null,
        val collate: String? = null,
        val columnMappings: Map<String, String> = emptyMap(),
        val dateTimeProvider: String? = null
)