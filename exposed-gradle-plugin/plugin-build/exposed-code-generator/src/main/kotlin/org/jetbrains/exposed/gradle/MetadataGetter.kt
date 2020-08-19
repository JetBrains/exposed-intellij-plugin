package org.jetbrains.exposed.gradle

import schemacrawler.schema.Table
import schemacrawler.schemacrawler.LoadOptionsBuilder
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility

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
        initDataSource(dataSource, user, password)
    }

    constructor(connection: () -> String, user: String? = null, password: String? = null) {
        dataSource = DatabaseConnectionSource(connection())
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
        val optionsBuilder = SchemaCrawlerOptionsBuilder.builder()
                .withLoadOptions(LoadOptionsBuilder.builder()
                        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
                        .toOptions()
                )


        val options = optionsBuilder.toOptions()
        val catalog = SchemaCrawlerUtility.getCatalog(dataSource.get(), options)
        return sortTablesByDependencies(catalog.schemas.flatMap { catalog.getTables(it) })
    }
}

