/*
package com.jetbrains.exposed.gradle.plugin.extension

import org.junit.jupiter.api.extension.*
import java.io.File

class TemporaryDir : BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private lateinit var tempDir: File

    override fun beforeEach(context: ExtensionContext?) {
        tempDir = createTempDir(suffix = "", prefix = "tempDir")
    }

    override fun afterEach(context: ExtensionContext?) {
        tempDir.deleteRecursively()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return (parameterContext.parameter.type == File::class.java) && (parameterContext.index == 0)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return tempDir
    }

}*/
