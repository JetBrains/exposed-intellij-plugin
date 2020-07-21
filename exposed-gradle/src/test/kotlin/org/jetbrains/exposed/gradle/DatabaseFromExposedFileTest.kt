package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.tests.DatabaseTestsBase
import org.jetbrains.exposed.gradle.tests.TestDB
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import java.nio.file.Path

// run tests from a .kt file, check against the same file
abstract class DatabaseFromExposedFileTest : DatabaseTestsBase() {
    protected fun testOnFile(
            testDataFilepath: Path,
            tables: List<Table>,
            tableName: String? = null,
            excludedDbList: List<TestDB> = emptyList()
    ) {
        withDb(excludeSettings = excludedDbList, statement = {
            for (table in tables) {
                SchemaUtils.drop(table)
                SchemaUtils.create(table)
            }
            checkDatabaseMetadataAgainstFile(it, generalTestDataPath, testDataFilepath, tableName)
            for (table in tables) {
                SchemaUtils.drop(table)
            }
        })
    }
}

