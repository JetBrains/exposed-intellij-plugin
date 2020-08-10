package org.jetbrains.exposed.gradle.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.gradle.*
import org.jetbrains.exposed.gradle.info.ColumnInfo
import org.jetbrains.exposed.gradle.info.TableInfo
import schemacrawler.schema.Column
import schemacrawler.schema.IndexType
import schemacrawler.schema.Table

class TableBuilder(
        table: Table,
        private val columnToPropertySpec: MutableMap<Column, PropertySpec>,
        private val columnToTableSpec: MutableMap<Column, TypeSpec>,
        private val columnNameToInitializerBlock: Map<String, String>,
        private val dialect: DBDialect? = null
) {
    private val tableInfo = TableInfo(table)
    private val builder = TypeSpec.objectBuilder(getObjectNameForTable(table))

    private fun generateExposedIdTableDeclaration() {
        val idColumn = tableInfo.idColumn!! // it's guaranteed to be non-null
        val idColumnInfo = ColumnInfo(idColumn)
        val idColumnClass = idColumnInfo.columnKClass
        if (tableInfo.superclass == IdTable::class) {
            builder.superclass(tableInfo.superclass.parameterizedBy(idColumnClass!!))
            builder.addSuperclassConstructorParameter("%S", tableInfo.tableName)
        } else {
            builder.superclass(tableInfo.superclass)
            builder.addSuperclassConstructorParameter("%S, %S", tableInfo.tableName, getColumnName(idColumn))
        }
    }

    fun generateExposedTableDeclaration() {
        if (tableInfo.idColumn != null) {
            generateExposedIdTableDeclaration()
        } else {
            builder.superclass(tableInfo.superclass)
            builder.addSuperclassConstructorParameter("%S", tableInfo.tableName)
        }
    }

    fun generateExposedTableColumns() {
        val idColumn = tableInfo.idColumn
        val columns = tableInfo.table.columns
        for (column in columns) {
            try {
                val columnMapping = columnNameToInitializerBlock[getColumnConfigName(column)]
                val columnBuilder = if (columnMapping != null) {
                    if (column == idColumn) {
                        MappedIdColumnBuilder(column, columnMapping, dialect)
                    } else {
                        MappedColumnBuilder(column, columnMapping, dialect)
                    }
                } else {
                    if (column == idColumn) {
                        IdColumnBuilder(column, dialect)
                    } else {
                        ColumnBuilder(column, dialect)
                    }
                }
                columnBuilder.generateExposedColumnInitializer(columnToPropertySpec, columnToTableSpec)
                val columnPropertySpec = columnBuilder.build()

                if (column != idColumn || tableInfo.superclass !in listOf(IntIdTable::class, LongIdTable::class, UUIDTable::class)) {
                    builder.addProperty(columnPropertySpec)
                }

                columnToPropertySpec[column] = columnPropertySpec
            } catch (e: UnsupportedTypeException) {
                logger.error("Unsupported type", e)
            }
        }
    }

    fun generateExposedTablePrimaryKey() {
        if (tableInfo.primaryKeyColumns.isEmpty() || tableInfo.superclass in listOf(IntIdTable::class, LongIdTable::class, UUIDTable::class)) {
            return
        }

        val primaryKeys = tableInfo.primaryKeyColumns.map {
            columnToPropertySpec[it]?.name
                    ?: throw ReferencedColumnNotFoundException("Primary key column ${it.fullName} not found.")
        }
        val primaryKey =
                PropertySpec.builder("primaryKey", ClassName("", "PrimaryKey"), KModifier.OVERRIDE)
                        .initializer(CodeBlock.of("%M(${primaryKeys.joinToString(", ")})", MemberName("", "PrimaryKey")))
                        .build()
        builder.addProperty(primaryKey)
    }

    fun generateExposedTableMulticolumnIndexes() {
        val indexes = tableInfo.table.indexes.filter { it.columns.size > 1 || it.indexType !in listOf(IndexType.other, IndexType.unknown)}
        if (indexes.isEmpty()) {
            return
        }

        builder.addInitializerBlock(buildCodeBlock {
            for (index in indexes) {
                if (tableInfo.primaryKeyColumns.containsAll(index.columns) && tableInfo.primaryKeyColumns.size == index.columns.size) {
                    continue
                }
                val name = getIndexName(index)
                val columns = index.columns.map {
                    columnToPropertySpec[it]?.name
                            ?: throw ReferencedColumnNotFoundException("Column ${it.fullName} definition not generated, can't create index.")
                }
                val indexType = indexTypeName[index.indexType]
                val indexTypeString = if (indexType != null) ", indexType = \"$indexType\"" else ""
                if (index.isUnique) {
                    add("%M(%S, ${columns.joinToString(", ")}$indexTypeString)", MemberName("", "uniqueIndex"), name)
                } else {
                    add("%M(%S, false, ${columns.joinToString(", ")}$indexTypeString)", MemberName("", "index"), name)
                }
            }
        })
    }

    // correct name
    private val indexTypeName = mapOf(
            IndexType.hashed to "HASH"
    )

    fun build(): TypeSpec {
        val exposedTable = builder.build()
        for (column in tableInfo.table.columns) {
            columnToTableSpec[column] = exposedTable
        }
        return exposedTable
    }
}