package org.jetbrains.exposed.gradle

import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility

// TODO parameters should include host, port
class MetadataGetter(
        databaseDriver: String,
        databaseName: String,
        user: String? = null,
        password: String? = null
) {
    private val dataSource: DatabaseConnectionSource = DatabaseConnectionSource("jdbc:$databaseDriver:$databaseName")
    init {
        if (user != null && password != null) {
            dataSource.userCredentials = SingleUseUserCredentials(user, password)
        }
    }

    // using the Table class from schemacrawler for now
    fun getTables(): List<Table> {
        val catalog = SchemaCrawlerUtility.getCatalog(
                dataSource.get(),
                SchemaCrawlerOptionsBuilder.builder().toOptions()
        )
        return sortTablesByDependencies(catalog.schemas.flatMap { catalog.getTables(it) })
    }
}

