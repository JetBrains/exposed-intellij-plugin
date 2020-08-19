package com.jetbrains.exposed.gradle.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test


class ExposedGradlePluginTest {
    @Test
    fun `correct task is present`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.jetbrains.exposed.gradle.plugin")

        assert(project.tasks.getByName("generateExposedCode") is ExposedGenerateCodeTask)
    }
}

