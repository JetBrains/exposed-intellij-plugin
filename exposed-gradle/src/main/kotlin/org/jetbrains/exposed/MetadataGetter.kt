package org.jetbrains.exposed

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.apache.commons.text.CaseUtils
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility
import java.math.BigDecimal
import java.sql.Blob
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass

private val numericArgumentsPattern = Pattern.compile("\\(([0-9]+([, ]*[0-9])*)\\)")

// using the Table class from schemacrawler for now
// TODO parameters should include host, port
private fun getTables(databaseDriver: String, databaseName: String, user: String? = null, password: String? = null): List<Table> {
    val dataSource = DatabaseConnectionSource("jdbc:$databaseDriver:$databaseName")
    if (user != null && password != null) {
        dataSource.userCredentials = SingleUseUserCredentials(user, password)
    }
    val catalog = SchemaCrawlerUtility.getCatalog(dataSource.get(), SchemaCrawlerOptionsBuilder.builder().toOptions())

    return catalog.schemas.flatMap { catalog.getTables(it) }
}

private fun toCamelCase(str: String, capitalizeFirst: Boolean = false) =
        CaseUtils.toCamelCase(str, capitalizeFirst, '_')

private fun generatePropertyForColumn(column: Column): PropertySpec {
    val packageName = "org.jetbrains.exposed.sql"
    val columnName = if (column.name.contains('_')) toCamelCase(column.name) else column.name

    fun columnInitializerCodeBlock(functionName: String, vararg arguments: Any): CodeBlock =
            if (arguments.isEmpty()) {
                CodeBlock.of("%M(\"${column.name}\")", MemberName(packageName, functionName))
            } else {
                CodeBlock.of("%M(\"${column.name}\", ${arguments.joinToString(", ")})", MemberName(packageName, functionName))
            }

    var columnInitializerBlock: CodeBlock? = null
    var columnType: KClass<*>? = null
    when (column.columnDataType.typeMappedClass) {
        Integer::class.javaObjectType -> {
            when (column.columnDataType.fullName.toLowerCase()) {
                "tinyint" -> {
                    columnInitializerBlock = columnInitializerCodeBlock("byte")
                    columnType = Byte::class
                }
                "smallint", "int2" -> {
                    columnInitializerBlock = columnInitializerCodeBlock("short")
                    columnType = Short::class
                }
                "int8" -> {
                    columnInitializerBlock = columnInitializerCodeBlock("long")
                    columnType = Long::class
                }
                else -> {
                    columnInitializerBlock = columnInitializerCodeBlock("integer")
                    columnType = Int::class
                }
            }
        }
        Long::class.javaObjectType -> {
            columnInitializerBlock = columnInitializerCodeBlock("long")
            columnType = Long::class
        }
        BigDecimal::class.java -> {
            columnInitializerBlock = columnInitializerCodeBlock("decimal", column.size, column.decimalDigits)
            columnType = BigDecimal::class
        }
        Float::class.javaObjectType-> {
            columnInitializerBlock = columnInitializerCodeBlock("float")
            columnType = Float::class
        }
        Double::class.javaObjectType -> {
            val name = column.columnDataType.fullName.toLowerCase()
            val matcher = numericArgumentsPattern.matcher(name)
            if (matcher.find() && (name.contains("decimal") || name.contains("numeric"))) {
                columnInitializerBlock = columnInitializerCodeBlock("decimal", matcher.group(1))
                columnType = BigDecimal::class
            } else {
                columnInitializerBlock = columnInitializerCodeBlock("double")
                columnType = Double::class
            }
        }
        Boolean::class.javaObjectType -> {
            columnInitializerBlock = columnInitializerCodeBlock("bool")
            columnType = Boolean::class
        }
        String::class.java -> {
            val name = column.columnDataType.fullName.toLowerCase()
            val matcher = numericArgumentsPattern.matcher(name)
            val size = if (matcher.find()) matcher.group(1).takeWhile { it.isDigit() }.toInt() else column.size
            columnInitializerBlock = when {
                name.contains("varchar") || name.contains("varying") -> columnInitializerCodeBlock("varchar", size)
                name.contains("char") -> columnInitializerCodeBlock("char", size)
                name.contains("text") -> columnInitializerCodeBlock("text")
                // TODO tell user about unsupported types
                else -> CodeBlock.of("")
            }
            columnType = String::class
        }
        Object::class.javaObjectType -> {
            columnInitializerBlock = when (column.columnDataType.fullName.toLowerCase()) {
                "uuid" -> {
                    columnType = UUID::class
                    columnInitializerCodeBlock("uuid")
                }
                else -> CodeBlock.of("") // TODO tell user about unsupported types
            }
        }
        Blob::class.javaObjectType -> {
            columnInitializerBlock = columnInitializerCodeBlock("blob")
            columnType = ExposedBlob::class
        }
        // TODO binary
    }

    // TODO throw a custom exception?
    if (columnInitializerBlock == null || columnType == null) {
        return PropertySpec.builder(columnName, org.jetbrains.exposed.sql.Column::class).build()
    }

    if (column.isAutoIncremented) {
        columnInitializerBlock = columnInitializerBlock.toBuilder().add(".autoIncrement()").build()
    }

    return PropertySpec.builder(columnName, org.jetbrains.exposed.sql.Column::class.parameterizedBy(columnType))
            .initializer(columnInitializerBlock).build()
}

private fun generateExposedTable(sqlTable: Table): TypeSpec {
    val primaryKeyColumns = sqlTable.columns.filter { it.isPartOfPrimaryKey }
    val idColumn = if (primaryKeyColumns.size == 1) primaryKeyColumns[0] else null
    val superclass = if (idColumn != null) {
        when (idColumn.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> IntIdTable::class
            Long::class.javaObjectType -> LongIdTable::class
            Object::class.javaObjectType ->
                if (idColumn.columnDataType.fullName.toLowerCase() == "uuid") UUIDTable::class else IdTable::class
            else -> IdTable::class
        }
    } else {
        org.jetbrains.exposed.sql.Table::class
    }

    val tableName = if (sqlTable.name.contains('_')) toCamelCase(sqlTable.name, capitalizeFirst = true) else sqlTable.name.capitalize()
    val tableObject = TypeSpec.objectBuilder(tableName)
    if (idColumn != null) {
        if (superclass == IdTable::class) {
            tableObject.superclass(superclass.parameterizedBy(idColumn.columnDataType.typeMappedClass.kotlin))
        } else {
            tableObject.superclass(superclass)
        }
        tableObject.addSuperclassConstructorParameter(
                "%S, %S",
                sqlTable.name,
                idColumn.name // to specify the id column name, which might not be "id"
        )
    } else {
        tableObject.superclass(superclass)
        tableObject.addSuperclassConstructorParameter(
                "%S",
                sqlTable.name
        )
    }
    for (column in sqlTable.columns) {
        if (column == idColumn) {
            continue
        }
        tableObject.addProperty(generatePropertyForColumn(column))
    }

    return tableObject.build()
}

fun generateExposedTablesForDatabase(
        databaseDriver: String,
        databaseName: String,
        user: String? = null,
        password: String? = null,
        tableName: String?
): FileSpec {
    val fileSpec = FileSpec.builder("", "${toCamelCase(databaseName, capitalizeFirst = true)}.kt")
    val tables = getTables(databaseDriver, databaseName, user, password)
    for (table in tables) {
        if (tableName != null && table.name != tableName) {
            continue
        }
        fileSpec.addType(generateExposedTable(table))
    }

    return fileSpec.build()
}


