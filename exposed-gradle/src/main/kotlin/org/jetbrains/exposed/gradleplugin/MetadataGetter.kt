package org.jetbrains.exposed.gradleplugin

import org.apache.commons.text.CaseUtils
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility
import java.math.BigDecimal
import java.util.regex.Pattern
import kotlin.reflect.KClass

private val numericArgumentsPattern = Pattern.compile("\\(([0-9]+([, ]*[0-9])*)\\)")

// using the Table class from schemacrawler for now
// TODO parameters should include host, port
// TODO possibly establish connection outside this method?
fun getTables(databaseDriver: String, databaseName: String, user: String? = null, password: String? = null): List<Table> {
    val dataSource = DatabaseConnectionSource("jdbc:$databaseDriver:$databaseName")
    if (user != null && password != null) {
        dataSource.userCredentials = SingleUseUserCredentials(user, password)
    }
    val catalog = SchemaCrawlerUtility.getCatalog(dataSource.get(), SchemaCrawlerOptionsBuilder.builder().toOptions())

    return catalog.schemas.flatMap { catalog.getTables(it) }
}

private fun getColumnFunction(column: Column): String {
    val template = "%s(\"${column.name}\")"
    var columnFunctionName: String? = null
    var columnType: KClass<*>? = null
    val templateWithArguments = "%s(\"${column.name}\", %s)"
    when (column.columnDataType.typeMappedClass) {
        Integer::class.javaObjectType -> {
            columnFunctionName = template.format("integer") + if (column.isAutoIncremented) ".autoIncrement()" else ""
            // TODO replace this with a class
            columnType = Int::class
        }
        Long::class.javaObjectType -> {
            columnFunctionName = template.format("long") + if (column.isAutoIncremented) ".autoIncrement()" else ""
            columnType = Long::class
        }
        BigDecimal::class.java -> {
            columnFunctionName = templateWithArguments.format("decimal", listOf(column.size, column.decimalDigits).joinToString(", "))
            columnType = BigDecimal::class
        }
        Float::class.javaObjectType-> {
            val name = column.columnDataType.fullName.toLowerCase()
            val matcher = numericArgumentsPattern.matcher(name)
            if (matcher.find() && (name.contains("decimal") || name.contains("numeric"))) {
                columnFunctionName = templateWithArguments.format("decimal", matcher.group(1))
                columnType = BigDecimal::class
            } else {
                columnFunctionName = template.format("float")
                columnType = Float::class
            }
        }
        Double::class.javaObjectType -> {
            columnFunctionName = template.format("double")
            columnType = Double::class
        }
        Boolean::class.javaObjectType -> {
            columnFunctionName = template.format("bool")
            columnType = Boolean::class
        }
        String::class.java -> {
            val name = column.columnDataType.fullName.toLowerCase()
            val matcher = numericArgumentsPattern.matcher(name)
            val size = if (matcher.find()) matcher.group(1).takeWhile { it.isDigit() }.toInt() else column.size
            columnFunctionName = when {
                name.contains("varchar") || name.contains("varying") -> templateWithArguments.format("varchar", size) // TODO no length varchar
                name.contains("char") -> templateWithArguments.format("char", size)
                name.contains("text") -> template.format("text")
                else -> "" // TODO tell user about unsupported types
            }
            columnType = String::class
        }
        Object::class.javaObjectType -> {
            columnFunctionName = when (column.columnDataType.fullName.toLowerCase()) {
                "uuid" -> template.format("uuid")
                else -> "" // TODO tell user about unsupported types
            }
            columnType = Object::class
        }
    }

    // TODO tell user; an exception or smth
    if (columnType == null || columnFunctionName == null) {
        return ""
    }

    return "val ${toCamelCase(column.name)}: Column<${columnType.simpleName}> = $columnFunctionName"
}

private fun toCamelCase(str: String, capitalizeFirst: Boolean = false) =
        CaseUtils.toCamelCase(str, capitalizeFirst, '_')


fun generateExposedTable(sqlTable: Table, indent: String = "    "): List<String> {
    val result = mutableListOf<String>()
    val tableType = if (sqlTable.columns.any { it.name == "id" && it.type.javaSqlType.defaultMappedClass == Integer::class.java }) {
        "IntIdTable"
    } else {
        "Table"
    }
    result.add("object ${toCamelCase(sqlTable.name, capitalizeFirst = true)} : $tableType() {")
    for (column in sqlTable.columns) {
        if (column.name == "id" && column.type.javaSqlType.defaultMappedClass == Integer::class.java) {
            continue
        }
        result.add(indent + getColumnFunction(column))
    }
    result.add("}")
    return result
}

