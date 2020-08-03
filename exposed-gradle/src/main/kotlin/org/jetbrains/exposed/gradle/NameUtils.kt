package org.jetbrains.exposed.gradle

import org.apache.commons.text.CaseUtils
import schemacrawler.schema.Column
import schemacrawler.schema.Table

fun String.toCamelCase(capitalizeFirst: Boolean = false): String =
        CaseUtils.toCamelCase(this, capitalizeFirst, '_')

// kotlin property names should be in camel case without capitalization
fun getPropertyNameForColumn(column: Column) = when {
    column.name.contains('_') -> column.name.toCamelCase()
    column.name.all { it.isUpperCase() } -> column.name.toLowerCase()
    else -> column.name.decapitalize()
}

// column names should be exactly as in the database; using lowercase for uniformity
fun getColumnName(column: Column) = column.name.toLowerCase()

fun getObjectNameForTable(table: Table) = when {
    table.name.contains('_') -> table.name.toCamelCase(true)
    table.name.all { it.isUpperCase() } -> table.name.toLowerCase().capitalize()
    else -> table.name.capitalize()
}

fun getTableName(table: Table) = table.name.toLowerCase()

// used in config files for mappings and such
fun getColumnConfigName(column: Column) = "${column.parent.name}.${column.name}".toLowerCase()