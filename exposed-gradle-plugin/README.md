#Exposed Code Generation Gradle Plugin

This Gradle plugin connects to a database and generates Exposed table definitions for all of its tables.

### Usage:

`gradle generateExposedCode`

### How to specify parameters:

* Using task properties:

`gradle generateExposedCode --connectionURL jdbc:postgresql:staff`

* Using system variables

`gradle generateExposedCode -DconnectionURL=jdbc:postgresql:staff`

* Using environment variables

* Using project variables

`gradle generateExposedCode -PconnectionURL=jdbc:postgresql:staff`

* Using a .properties file and specifying its filepath

```kotlin
exposedCodeGeneratorConfig {
    propertiesFilename = "exposedPluginProps.properties"
}
```

`exposedPluginProps.properties:`
```
connectionURL=jdbc:postgresql:testone?user=postgres&password=testing
```

* Using a task configuration in a `build.gradle` file

```kotlin
exposedCodeGeneratorConfig {
    databaseDriver = "postgresql"
    databaseName = "pltest"
    user = "root"
    configFilepath = "exposedCodeGeneratorConfig.yml"
}
```

It is strongly recommended to avoid mixing different ways of specifying parameters.

###Database connection parameters:

1. `connectionURL` -- connection URL as used with JDBC (e.g. `jdbc:postgresql://localhost:12346/user=postgres&password=`)

2. Specifying each connection parameter:
    1. `databaseDriver`, as used with JDBC (e.g. `postgresql`, `h2`)
    2. `databaseName` 
    3. `user`
    4. `password`
    5. `host` (IPv4)
    6. `port`
    7. `ipv6Host`
    
All of those parameters are optional; however, the expected behavior is that the user does not mix a `connectionURL` with other parameters.

### Exposed code generation parameters:

1. `configFilename` -- a path to a `.yml` config file

2. Specifying each configuration parameter:
    1. `packageName` -- the name of the package the generated files belong to
    2. `generateSingleFile` -- set to `true` if all tables are to be placed in the same file; otherwise a separate file is created for each table, titled same as table name in camel case
    3. `generatedFileName` -- specify only when `generateSingleFile = true`; the name of the file to be generated. Default filename is `GeneratedTables.kt`
    4. `collate` -- specify collation method for all string (`char`/`varchar`/`text`) columns in the database (e.g. `NOCASE` for SQLite)
    5. `columnMappings` -- in case the code generator is unable to provide a satisfactory definition for the column, it's possible to provide its definition directly in form of Exposed code (e.g. in order to map certain `Double` columns to `float`, use the following: `mapped_column_table.float_column: float("float_column")`; the column should be addressed as `[table_name].[column_name]`)
    
### Output directory:

By default, the generated files are written to `build/tables` directory (in case of a single file generation, the filename is `GeneratedTables.kt`). The parameter is `outputDirectory` and it can be set like all the other ones.
     
    
    