package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import java.nio.file.Path

// run tests from a sql script, check against a kt file
abstract class DatabaseFromScriptTest : DatabaseTestsBase() {
    protected fun testFromScriptAgainstKtFile(
            scriptFilepath: Path,
            testDataFilepath: Path,
            tableName: String? = null,
            excludedDbList: List<TestDB> = emptyList()
    ) {
        withDb(excludeSettings = excludedDbList, statement = {
            val script = scriptFilepath.toFile().readText()
            val splitResults = script.split(Regex("((?<=INSERT)|(?=INSERT))|((?<=CREATE)|(?=CREATE))|((?<=DROP)|(?=DROP))|((?<=--)|(?=--))")).filterNot { it.isBlank() }
            val commands = mutableListOf<String>()
            for (i in splitResults.indices step 2) {
                commands.add("${splitResults[i]} ${splitResults[i + 1]}")
            }
            commands.forEach { exec(it) }
            commit()
            checkDatabaseMetadataAgainstFile(it, resourcesTestDataPath, testDataFilepath, tableName)
        })
    }
}

