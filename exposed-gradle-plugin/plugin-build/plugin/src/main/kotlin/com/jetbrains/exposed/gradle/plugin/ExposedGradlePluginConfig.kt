package com.jetbrains.exposed.gradle.plugin

data class ExposedGradlePluginConfig(
        val databaseDriver: String? = null,
        val databaseName: String? = null,
        val user: String? = null,
        val password: String? = null,
        val host: String? = null,
        val port: String? = null,
        val ipv6Host: String? = null,
        val connectionURL: String? = null,
        val packageName: String? = null,
        val generateSingleFile: Boolean = true,
        val generatedFileName: String? = null,
        val collate: String? = null,
        val columnMappings: Map<String, String> = emptyMap()
)