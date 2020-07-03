package org.jetbrains.exposed.gradleplugin

import org.apache.commons.text.CaseUtils
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials
import schemacrawler.utility.SchemaCrawlerUtility
import java.math.BigDecimal

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
    val templateWithArguments = "%s(\"${column.name}\", %s)"
    return when (column.columnDataType.javaSqlType.defaultMappedClass) {
        Integer::class.javaObjectType ->
            template.format("integer") + if (column.isAutoIncremented) ".autoIncrement()" else ""
        Long::class.javaObjectType ->
            template.format("long") + if (column.isAutoIncremented) ".autoIncrement()" else ""
        BigDecimal::class.java ->
            templateWithArguments.format("decimal", listOf(column.size, column.decimalDigits).joinToString(", "))
        Float::class.javaObjectType, Double::class.javaObjectType -> template.format("float")
        Boolean::class.javaObjectType -> template.format("bool")
        String::class.java -> when (column.columnDataType.fullName.toLowerCase()) {
            "bpchar", "char", "character" -> templateWithArguments.format("char", column.size)
            "varchar" -> templateWithArguments.format("varchar", column.size) // TODO no length varchar
            "text" -> template.format("text")
            else -> "" // TODO tell user about unsupported types even though we support all text so this shouldn't happen?
        }
        Object::class.javaObjectType -> when (column.columnDataType.fullName.toLowerCase()) {
            "uuid" -> template.format("uuid")
            else -> "" // TODO tell user about whatever this is
        }
        else -> "" // TODO tell user about unsupported types
    }
}

private fun toCamelCase(str: String) = CaseUtils.toCamelCase(str, false, '_')


fun generateExposedTable(sqlTable: Table, indent: String = "    "): List<String> {
    val result = mutableListOf<String>()
    val tableType = if (sqlTable.columns.any { it.name == "id" && it.type.javaSqlType.defaultMappedClass == Integer::class.java }) {
        "IntIdTable"
    } else {
        "Table"
    }
    result.add("object ${toCamelCase(sqlTable.name)} : $tableType() {")
    for (column in sqlTable.columns) {
        if (column.name == "id" && column.type.javaSqlType.defaultMappedClass == Integer::class.java) {
            continue
        }
        result.add("${indent}val ${toCamelCase(column.name)} = ${getColumnFunction(column)}")
    }
    result.add("}")
    return result
}

