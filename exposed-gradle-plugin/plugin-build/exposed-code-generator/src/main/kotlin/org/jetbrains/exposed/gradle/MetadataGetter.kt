package org.jetbrains.exposed.gradle

import schemacrawler.crawl.SchemaCrawler
import schemacrawler.schema.Table
import schemacrawler.schemacrawler.*
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials

/**
 * Connects to a database and retrieves its tables.
 */
class MetadataGetter {
    private val dataSource: DatabaseConnectionSource

    constructor(
            databaseDriver: String,
            databaseName: String,
            user: String? = null,
            password: String? = null,
            host: String? = null,
            port: String? = null,
            ipv6Host: String? = null,
            additionalProperties: Map<String, String>? = null
    ) {
        val hostPortString = buildString {
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
        dataSource = DatabaseConnectionSource("jdbc:$databaseDriver:$hostPortString$databaseName", additionalProperties.orEmpty())
        initDataSource(dataSource, user, password)
    }

    constructor(connection: () -> String, user: String? = null, password: String? = null, additionalProperties: Map<String, String>? = null) {
        dataSource = DatabaseConnectionSource(connection(), additionalProperties.orEmpty())
        initDataSource(dataSource, user, password)
    }

    private fun initDataSource(dataSource: DatabaseConnectionSource, user: String?, password: String?) {
        if (user != null && password != null) {
            dataSource.userCredentials = SingleUseUserCredentials(user, password)
        }
        // to prevent exceptions at driver registration
        val driver = getDriver(dataSource.connectionUrl)
        Class.forName(driver).getDeclaredConstructor().newInstance()
    }

    private fun getDriver(url: String) = when {
        url.startsWith("jdbc:h2") -> "org.h2.Driver"
        url.startsWith("jdbc:postgresql") -> "org.postgresql.Driver"
        url.startsWith("jdbc:pgsql") -> "com.impossibl.postgres.jdbc.PGDriver"
        url.startsWith("jdbc:mysql") -> "com.mysql.cj.jdbc.Driver"
        url.startsWith("jdbc:mariadb") -> "org.mariadb.jdbc.Driver"
        url.startsWith("jdbc:oracle") -> "oracle.jdbc.OracleDriver"
        url.startsWith("jdbc:sqlite") -> "org.sqlite.JDBC"
        url.startsWith("jdbc:sqlserver") -> "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        else -> error("Database driver not found for $url")
    }

    /**
     * Returns tables from the database connected via [dataSource].
     */
    fun getTables(): List<Table> {
        val options = SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                .withLoadOptions(LoadOptionsBuilder.builder()
                        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
                        .toOptions()
                )


        val connection = dataSource.get()
        val retrievalOptions = SchemaRetrievalOptionsBuilder.builder().fromConnnection(connection).toOptions()
        val catalog = SchemaCrawler(connection, retrievalOptions, options).crawl()
//        return sortTablesByDependencies(catalog.schemas.flatMap { catalog.getTables(it) })
        return catalog.schemas.flatMap { catalog.getTables(it) }
    }
}

