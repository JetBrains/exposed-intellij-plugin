package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.dao.id.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.slf4j.LoggerFactory
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.reflect.KClass

// TODO support schemas
class ExposedCodeGenerator(private val tables: List<Table>) {

    // column name to its property spec
    private val processedColumns: LinkedHashMap<String, PropertySpec> = LinkedHashMap()
    // column name to its parent table spec
    private val columnsToTables: HashMap<String, TypeSpec> = HashMap()


    private fun generateUnsupportedTypeErrorMessage(column: Column) =
            "Unable to map column ${column.name} of type ${column.columnDataType.fullName} to an Exposed column object"

    private fun columnInitializerCodeBlock(
            columnName: String,
            packageName: String,
            functionName: String,
            vararg arguments: Any
    ): CodeBlock = if (arguments.isEmpty()) {
        CodeBlock.of("%M(\"$columnName\")", MemberName(packageName, functionName))
    } else {
        CodeBlock.of("%M(\"$columnName\", ${arguments.joinToString(", ")})", MemberName(packageName, functionName))
    }

    private data class ColumnDefinition(val columnKClass: KClass<*>, val columnInitializationBlock: CodeBlock)

    private fun generateColumnDefinition(column: Column): ColumnDefinition {
        val columnName = getColumnName(column)

        var columnInitializerBlock: CodeBlock? = null
        var columnType: KClass<*>? = null

        fun initializeColumnParameters(columnTypeClass: KClass<*>, functionName: String, vararg arguments: Any) {
            columnInitializerBlock = columnInitializerCodeBlock(columnName, exposedPackageName, functionName, *arguments)
            columnType = columnTypeClass
        }


        when (column.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> {
                when (column.columnDataType.fullName.toLowerCase()) {
                    "tinyint" -> initializeColumnParameters(Byte::class, "byte")
                    "smallint", "int2" -> initializeColumnParameters(Short::class, "short")
                    "int8" -> initializeColumnParameters(Long::class, "long")
                    else -> initializeColumnParameters(Int::class, "integer")
                }
            }
            Long::class.javaObjectType -> initializeColumnParameters(Long::class, "long")
            BigDecimal::class.java ->
                initializeColumnParameters(BigDecimal::class, "decimal", column.size, column.decimalDigits)
            Float::class.javaObjectType-> initializeColumnParameters(Float::class, "float")
            Double::class.javaObjectType -> {
                val name = column.columnDataType.fullName.toLowerCase()
                val matcher = numericArgumentsPattern.matcher(name)
                if (matcher.find() && (name.contains("decimal") || name.contains("numeric"))) {
                    initializeColumnParameters(BigDecimal::class, "decimal", matcher.group(1))
                } else {
                    initializeColumnParameters(Double::class, "double")
                }
            }
            Boolean::class.javaObjectType -> initializeColumnParameters(Boolean::class, "bool")
            String::class.java -> {
                val name = column.columnDataType.fullName.toLowerCase()
                val matcher = numericArgumentsPattern.matcher(name)
                val size = if (matcher.find()) matcher.group(1).takeWhile { it.isDigit() }.toInt() else column.size
                when {
                    name.contains("varchar") || name.contains("varying") -> initializeColumnParameters(String::class, "varchar", size)
                    name.contains("char") -> initializeColumnParameters(String::class, "char", size)
                    name.contains("text") -> initializeColumnParameters(String::class, "text")
                    else -> throw MetadataUnsupportedTypeException(generateUnsupportedTypeErrorMessage(column))
                }
            }
            Clob::class.javaObjectType -> {
                initializeColumnParameters(String::class, "text")
            }
            Object::class.javaObjectType -> {
                when (column.columnDataType.fullName.toLowerCase()) {
                    "uuid" -> initializeColumnParameters(UUID::class, "uuid")
                    else -> throw MetadataUnsupportedTypeException(generateUnsupportedTypeErrorMessage(column))
                }
            }
            Blob::class.javaObjectType -> initializeColumnParameters(ExposedBlob::class, "blob")
            UUID::class.javaObjectType -> initializeColumnParameters(UUID::class, "uuid")
            else -> {
                val name = column.columnDataType.fullName.toLowerCase()
                when {
                    name.contains("uuid") -> initializeColumnParameters(UUID::class, "uuid")
                    // can be 'varbinary'
                    name.contains("binary") || name.contains("bytea") -> initializeColumnParameters(ByteArray::class, "binary", column.size)
                }
            }
        }

        if (columnInitializerBlock == null || columnType == null) {
            throw MetadataUnsupportedTypeException(generateUnsupportedTypeErrorMessage(column))
        }

        if (column.isAutoIncremented) {
            columnInitializerBlock = columnInitializerBlock!!.append(CodeBlock.of(
                    ".%M()",
                    MemberName(exposedPackageName, "autoIncrement")
            ))
        }

        if (column.referencedColumn != null) {
            val referencedColumnProperty = processedColumns[column.referencedColumn.fullName]
                    ?: throw MetadataReferencedColumnNotFoundException(
                            "Column ${column.referencedColumn.fullName} referenced by ${column.fullName} not found."
                    )
            val referencedColumnTable = columnsToTables[column.referencedColumn.fullName]
            columnInitializerBlock = if (column.parent == column.referencedColumn.parent) {
                columnInitializerBlock!!.append(CodeBlock.of(
                        ".%M(%N)",
                        MemberName(exposedPackageName, "references"),
                        referencedColumnProperty
                ))
            } else {
                columnInitializerBlock!!.append(CodeBlock.of(
                        ".%M(%N.%N)",
                        MemberName(exposedPackageName, "references"),
                        referencedColumnTable, // should be not null
                        referencedColumnProperty
                ))
            }
        }

        return ColumnDefinition(columnType!!, columnInitializerBlock!!)
    }

    private fun CodeBlock.append(codeBlock: CodeBlock): CodeBlock {
        return this.toBuilder().add(codeBlock).build()
    }

    private fun generatePropertyForColumn(column: Column): PropertySpec {
        val columnVariableName = getPropertyNameForColumn(column)
        val columnDefinition = generateColumnDefinition(column)

        return PropertySpec.Companion.builder(
                columnVariableName,
                org.jetbrains.exposed.sql.Column::class.parameterizedBy(columnDefinition.columnKClass)
        ).initializer(columnDefinition.columnInitializationBlock).build()
    }

    private fun generateExposedTable(sqlTable: Table): TypeSpec {
        val primaryKeyColumns = sqlTable.columns.filter { it.isPartOfPrimaryKey }
        val idColumn = if (primaryKeyColumns.size == 1) primaryKeyColumns[0] else null
        val superclass = if (idColumn != null) {
            when (idColumn.columnDataType.typeMappedClass) {
                Integer::class.javaObjectType -> IntIdTable::class
                Long::class.javaObjectType -> LongIdTable::class
                else -> if (idColumn.columnDataType.fullName.equals("uuid", ignoreCase = true)) UUIDTable::class else IdTable::class
            }
        } else {
            org.jetbrains.exposed.sql.Table::class
        }

        val tableObjectName = getObjectNameForTable(sqlTable)
        val tableName = getTableName(sqlTable)
        val tableObject = TypeSpec.objectBuilder(tableObjectName)
        if (idColumn != null) {
            if (superclass == IdTable::class) {
                tableObject.superclass(superclass.parameterizedBy(idColumn.columnDataType.typeMappedClass.kotlin))
                tableObject.addSuperclassConstructorParameter(
                        "%S",
                        tableName
                )
            } else {
                tableObject.superclass(superclass)
                tableObject.addSuperclassConstructorParameter(
                        "%S, %S",
                        tableName,
                        getColumnName(idColumn) // to specify the id column name, which might not be "id"
                )
            }

        } else {
            tableObject.superclass(superclass)
            tableObject.addSuperclassConstructorParameter(
                    "%S",
                    tableName
            )
        }
        for (column in sqlTable.columns) {
            if (column == idColumn) {
                val columnDefinition = generateColumnDefinition(column)
                val columnPropertyBuilder = PropertySpec.Companion.builder("id", org.jetbrains.exposed.sql.Column::class.asClassName()
                        .parameterizedBy(EntityID::class.parameterizedBy(columnDefinition.columnKClass)))
                if (superclass == IdTable::class) {
                    val property = columnPropertyBuilder
                            .addModifiers(KModifier.OVERRIDE)
                            .getter(FunSpec.getterBuilder()
                                    .addCode(CodeBlock.of("return "))
                                    .addCode(columnDefinition.columnInitializationBlock)
                                    .addCode(".%M()", MemberName(exposedPackageName, "entityId"))
                                    .build()
                            )
                            .build()
                    tableObject.addProperty(property)
                    processedColumns[column.fullName] = property
                } else {
                    processedColumns[column.fullName] = columnPropertyBuilder.build()
                }
                continue
            }
            try {
                val columnProperty = generatePropertyForColumn(column)
                tableObject.addProperty(columnProperty)
                processedColumns[column.fullName] = columnProperty
            } catch (e: MetadataUnsupportedTypeException) {
                // TODO log the stacktrace or not? technically this should be readable by the client, so... not?
                logger.error("Unsupported type", e)
            }
        }

        val table = tableObject.build()
        for (column in sqlTable.columns) {
            columnsToTables[column.fullName] = table
        }

        return table
    }


    fun generateExposedTables(databaseName: String): FileSpec {
        val fileSpec = FileSpec.builder("", "${databaseName.toCamelCase(true)}.kt")
        tables.forEach { fileSpec.addType(generateExposedTable(it)) }

        return fileSpec.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger("MetadataGetterLogger")

        private val numericArgumentsPattern = Pattern.compile("\\(([0-9]+([, ]*[0-9])*)\\)")

        private val exposedPackageName = org.jetbrains.exposed.sql.Table::class.java.packageName
    }
}