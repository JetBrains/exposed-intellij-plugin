package org.jetbrains.exposed.gradle

import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility
import java.lang.StringBuilder

// TODO parameters should include host, port
class MetadataGetter {
    private val dataSource: DatabaseConnectionSource

    constructor(
            databaseDriver: String,
            databaseName: String,
            user: String? = null,
            password: String? = null,
            host: String? = null,
            port: String? = null,
            ipv6Host: String? = null
    ) {
        val hostPortString = StringBuilder().apply {
            if (ipv6Host != null || host != null) {
                append("//")
                if (ipv6Host != null) {
                    append("[$ipv6Host]")
                } else {
                    append(host)
                }
                if (port != null) {
                    append(":$port")
                }
                append("/")
            }
        }
        dataSource = DatabaseConnectionSource("jdbc:$databaseDriver:$hostPortString$databaseName")
        if (user != null && password != null) {
            dataSource.userCredentials = SingleUseUserCredentials(user, password)
        }
    }

    constructor(connection: () -> String, user: String? = null, password: String? = null) {
        dataSource = DatabaseConnectionSource(connection())
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

