package org.jetbrains.exposed.gradle.info

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.gradle.builders.TableBuilderData
import org.jetbrains.exposed.gradle.getTableName
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.util.*

data class TableInfo(val table: Table, private val data: TableBuilderData) {
    val primaryKeyColumns: List<Column> = if (table.hasPrimaryKey()) table.primaryKey.columns else emptyList()
    val idColumn: Column? = if (primaryKeyColumns.size == 1) {
        val column = primaryKeyColumns[0]
        val columnInfo = ColumnInfo(column, data)
        val columnExposedClass = columnInfo.columnKClass
        if ((columnExposedClass == Int::class || columnExposedClass == Long::class) && !column.isAutoIncremented) {
            null
        } else {
            column
        }
    } else {
        null
    }
    val tableName = getTableName(table)
    val superclass = if (idColumn == null) {
        org.jetbrains.exposed.sql.Table::class
    } else {
        when (ColumnInfo(idColumn, data).columnKClass) {
            Int::class -> IntIdTable::class
            Long::class -> LongIdTable::class
            UUID::class -> UUIDTable::class
            else -> IdTable::class
        }
    }
}