package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.gradle.databases.*
import org.jetbrains.exposed.gradle.tests.TestDB
import org.junit.Test
import java.nio.file.Paths

// run tests from a .kt file, check against the same file
class ExposedCodeGeneratorFromExposedTest : DatabaseFromExposedFileTest() {
    @Test
    fun integerTypes() {
        testOnFile(
                Paths.get("IntegerTypes.kt"),
                listOf(IntegerTypes),
                "integer_types",
                excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    // no 'tinyint' in postgres, only 2, 4, or 8 bytes
    fun integerTypesPostgres() {
        testOnFile(
                Paths.get("postgresql", "IntegerTypes.kt"),
                listOf(IntegerTypes),
                "integer_types",
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun decimalTypes() {
        testOnFile(Paths.get("DecimalTypes.kt"), listOf(DecimalTypes), "decimal_types")
    }

    // why does exposed map a float column to double?
/*    @Test
    fun floatingPointTypes() {
        testOnFile(Paths.get("FloatingPointTypes.kt"), listOf(FloatingPointTypes), excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE))
    }*/

    @Test
    fun charTypes() {
        testOnFile(Paths.get("CharTypes.kt"), listOf(CharTypes), "char_types")
    }

    @Test
    fun miscTypes() {
        testOnFile(
                Paths.get("MiscTypes.kt"),
                listOf(MiscTypes),
                "misc_types",
                excludedDbList = listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    //@Test
    // datetime() gets mapped to timestamp and is indistinguishable from timestamp()
    // duration() gets mapped to long and is indistinguishable from it
    //
    // SQLite doesn't have datetime types and uses text/real/integer instead,
    // making it indistinguishable from genuine text/real/integer columns
    /*fun testDateTimeTypes() {
        testOnFile(
                Paths.get("DateTimeTypes.kt"),
                listOf(DateTimeTypes),
                excludedDbList = listOf(TestDB.SQLITE)
        )
    }*/

    @Test
    // can't specify the length in postgres
    fun miscTypesPostgres() {
        testOnFile(
                Paths.get("postgresql", "MiscTypes.kt"),
                listOf(MiscTypes),
                "misc_types",
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun idTables() {
        testOnFile(
                Paths.get("IdTables.kt"),
                listOf(Sample1, Sample2, Sample3, Sample4),
                excludedDbList = listOf(TestDB.SQLITE, TestDB.MYSQL)
        )
    }

    @Test
    // Exposed long gets mapped to Integer and there's no way to retrieve Long back
    // Exposed UUID gets mapped to binary
    fun longIdTableSQLite() {
        testOnFile(
                Paths.get("sqlite", "IdTables.kt"),
                listOf(Sample1, Sample2, Sample4),
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE)
        )
    }

    // Exposed UUID gets mapped to binary
    // and I don't know what to do about that yet
    /*@Test
    fun uuidTableMySQL() {
        testOnFile(
                Paths.get("mysql", "IdTables.kt"),
                listOf(Sample1, Sample2, Sample4),
                excludedDbList = TestDB.enabledInTests() - listOf(TestDB.MYSQL)
        )
    }*/

    @Test
    // PostgreSQL requires the referenced column to have a 'unique' constraint,
    // but we don't have such constraints in Exposed, we only have primary keys
    // MySQL doesn't allow referencing a column on DB creation, only foreign keys
    fun selfForeignKey() {
        testOnFile(
                Paths.get("SelfForeignKeyTable.kt"),
                listOf(SelfForeignKeyTable),
                excludedDbList = listOf(TestDB.MYSQL, TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun foreignKey() {
        testOnFile(
                Paths.get("ForeignKeyTable.kt"),
                listOf(Sample, SampleRef),
                excludedDbList = listOf(TestDB.MYSQL, TestDB.POSTGRESQL, TestDB.POSTGRESQLNG)
        )
    }

    @Test
    fun nullableTypes() {
        testOnFile(
                Paths.get("NullableTypes.kt"),
                listOf(NullableTypes)
        )
    }

    @Test
    fun primaryKey() {
        testOnFile(Paths.get("PrimaryKeyTable.kt"), listOf(SinglePrimaryKeyTable))
    }

    @Test
    fun compositePrimaryKey() {
        testOnFile(Paths.get("CompositePrimaryKeyTable.kt"), listOf(CompositePrimaryKeyTable))
    }
}

class ExposedCodeGeneratorFromScriptTest : DatabaseFromScriptTest() {
    @Test
    // this test can be used for making sure all table references are established correctly
    fun chinook() = testFromScriptAgainstKtFile(
            Paths.get(resourcesTestDataPath.toString(), "sqlite", "chinook.sql"),
            Paths.get("sqlite", "Chinook.kt"),
            excludedDbList = TestDB.enabledInTests() - listOf(TestDB.SQLITE)
    )

}