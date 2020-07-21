package org.jetbrains.exposed.gradle

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.dao.id.*
import org.jetbrains.exposed.sql.`java-time`.JavaLocalDateColumnType
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.slf4j.LoggerFactory
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.time.LocalDate
import java.time.LocalDateTime
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

    private fun generateColumnInitializerCodeBlock(
            columnName: String,
            packageName: String,
            functionName: String,
            vararg arguments: Any
    ): CodeBlock = if (arguments.isEmpty()) {
        // TODO fix this
        CodeBlock.of("%M(\"$columnName\")", MemberName(packageName, functionName))
    } else {
        CodeBlock.of("%M(\"$columnName\", ${arguments.joinToString(", ")})", MemberName(packageName, functionName))
    }

    private data class ColumnDefinition(val columnKClass: KClass<*>, val columnInitializationBlock: CodeBlock)

    private fun generateColumnDefinition(column: Column): ColumnDefinition {
        val columnName = getColumnName(column)

        var columnInitializerBlock: CodeBlock? = null
        var columnType: KClass<*>? = null

        fun initializeColumnParameters(columnTypeClass: KClass<*>, functionName: String, vararg arguments: Any, packageName: String = "") {
            columnInitializerBlock = generateColumnInitializerCodeBlock(columnName, packageName, functionName, *arguments)
            columnType = columnTypeClass
        }


        when (column.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> {
                when (column.columnDataType.name.toLowerCase()) {
                    "tinyint" -> initializeColumnParameters(Byte::class, "byte")
                    "smallint", "int2" -> initializeColumnParameters(Short::class, "short")
                    "int8" -> initializeColumnParameters(Long::class, "long")
                    else -> initializeColumnParameters(Int::class, "integer")
                }
            }
            Long::class.javaObjectType -> initializeColumnParameters(Long::class, "long")
            BigDecimal::class.java -> {
                // TODO work out a constant for all databases perhaps?
                // or rewrite for different dbs
                val precision = if (column.size >= 0 && column.size < MaxSize.MAX_DECIMAL_PRECISION) {
                    column.size
                } else {
                    MaxSize.MAX_DECIMAL_PRECISION
                }
                val scale = when {
                    // it's unlikely that this is to ever happen but just to cover the possibility
                    column.decimalDigits > MaxSize.MAX_DECIMAL_SCALE -> MaxSize.MAX_DECIMAL_SCALE
                    column.decimalDigits < 0 -> 0
                    else -> column.decimalDigits
                }
                initializeColumnParameters(BigDecimal::class, "decimal", precision, scale)
            }
            Float::class.javaObjectType-> initializeColumnParameters(Float::class, "float")
            Double::class.javaObjectType -> {
                val name = column.columnDataType.name.toLowerCase()
                val matcher = numericArgumentsPattern.matcher(name)
                if (matcher.find() && (name.contains("decimal") || name.contains("numeric"))) {
                    initializeColumnParameters(
                            BigDecimal::class,
                            "decimal",
                            *matcher.group(1).split(",").map { it.trim() }.toTypedArray()
                    )
                } else {
                    initializeColumnParameters(Double::class, "double")
                }
            }
            Boolean::class.javaObjectType -> initializeColumnParameters(Boolean::class, "bool")
            String::class.java -> {
                val name = column.columnDataType.name.toLowerCase()
                val matcher = numericArgumentsPattern.matcher(name)
                val size = when {
                    matcher.find() -> matcher.group(1).takeWhile { it.isDigit() }.toInt()
                    column.size > 0 -> column.size
                    else -> MaxSize.MAX_VARCHAR_SIZE
                }
                when {
                    name.contains("varchar") || name.contains("varying") -> initializeColumnParameters(String::class, "varchar", size)
                    name.contains("char") -> initializeColumnParameters(String::class, "char", size)
                    name.contains("text") -> initializeColumnParameters(String::class, "text")
                    name.contains("time") ->
                        initializeColumnParameters(LocalDateTime::class, "datetime", packageName = exposedDateTimePackageName)
                    name.contains("date") ->
                        initializeColumnParameters(LocalDate::class, "date", packageName = exposedDateTimePackageName)
                    name.contains("binary") || name.contains("bytea") ->
                        initializeColumnParameters(ByteArray::class, "binary", size)
                    // TODO timestamp, duration
                    else -> throw MetadataUnsupportedTypeException(generateUnsupportedTypeErrorMessage(column))
                }
            }
            Clob::class.javaObjectType -> {
                initializeColumnParameters(String::class, "text")
            }
            Object::class.javaObjectType -> {
                when (column.columnDataType.name.toLowerCase()) {
                    "uuid" -> initializeColumnParameters(UUID::class, "uuid")
                    else -> throw MetadataUnsupportedTypeException(generateUnsupportedTypeErrorMessage(column))
                }
            }
            Blob::class.javaObjectType -> initializeColumnParameters(ExposedBlob::class, "blob")
            UUID::class.javaObjectType -> initializeColumnParameters(UUID::class, "uuid")
            else -> {
                val name = column.columnDataType.name.toLowerCase()
                when {
                    name.contains("uuid") -> initializeColumnParameters(UUID::class, "uuid")
                    // can be 'varbinary'
                    name.contains("binary") || name.contains("bytea") -> {
                        val size = if (column.size > 0) column.size else MaxSize.MAX_BINARY
                        initializeColumnParameters(ByteArray::class, "binary", size)
                    }
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
            val blockToAppend = if (column.parent == column.referencedColumn.parent) {
                CodeBlock.of(
                        ".%M(%N)",
                        MemberName(exposedPackageName, "references"),
                        referencedColumnProperty
                )
            } else {
                CodeBlock.of(
                        ".%M(%N.%N)",
                        MemberName(exposedPackageName, "references"),
                        referencedColumnTable, // should be not null
                        referencedColumnProperty
                )
            }
            columnInitializerBlock = columnInitializerBlock!!.append(blockToAppend)
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

    // TODO extension method?
    private fun addColumns(table: Table, tableObject: TypeSpec.Builder, idColumn: Column? = null) {
        for (column in table.columns) {
            if (column == idColumn) {
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
    }

    private fun generateIdTable(table: Table, idColumn: Column): TypeSpec.Builder {
        val columnDefinition = generateColumnDefinition(idColumn)
        val columnPropertyBuilder = PropertySpec.builder("id", org.jetbrains.exposed.sql.Column::class.asClassName()
                .parameterizedBy(EntityID::class.parameterizedBy(columnDefinition.columnKClass)))

        val superclass = when (idColumn.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> IntIdTable::class
            Long::class.javaObjectType -> LongIdTable::class
            else -> if (idColumn.columnDataType.name.equals("uuid", ignoreCase = true)) UUIDTable::class else IdTable::class
        }

        val tableObjectName = getObjectNameForTable(table)
        val tableName = getTableName(table)
        val tableObjectBuilder = TypeSpec.objectBuilder(tableObjectName)

        if (superclass == IdTable::class) {
            tableObjectBuilder.superclass(superclass.parameterizedBy(columnDefinition.columnKClass))
            tableObjectBuilder.addSuperclassConstructorParameter(
                    "%S",
                    tableName
            )
            val primaryKey = PropertySpec.builder(
                    "primaryKey",
                    ClassName("", "PrimaryKey").copy(nullable = true),
                    KModifier.OVERRIDE
            )
                    .delegate(CodeBlock.builder()
                            .beginControlFlow("lazy")
                            .add("super.primaryKey ?: PrimaryKey(id)")
                            .add("\n")
                            .endControlFlow()
                            .build()
                    )
                    .build()
            tableObjectBuilder.addProperty(primaryKey)
            val idProperty = columnPropertyBuilder
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(columnDefinition.columnInitializationBlock
                            .append(CodeBlock.of(".%M()", MemberName("", "entityId")))
                    )
                    .build()
            tableObjectBuilder.addProperty(idProperty)
            processedColumns[idColumn.fullName] = idProperty
        } else {
            tableObjectBuilder.superclass(superclass)
            tableObjectBuilder.addSuperclassConstructorParameter(
                    "%S, %S",
                    tableName,
                    getColumnName(idColumn) // to specify the id column name, which might not be "id"
            )
            processedColumns[idColumn.fullName] = columnPropertyBuilder.build()
        }

        addColumns(table, tableObjectBuilder, idColumn)
        return tableObjectBuilder
    }

    private fun generateExposedTable(table: Table): TypeSpec {
        val primaryKeyColumns = table.columns.filter { it.isPartOfPrimaryKey }
        val idColumn = if (primaryKeyColumns.size == 1) primaryKeyColumns[0] else null
        val tableObjectBuilder: TypeSpec.Builder
        if (idColumn != null) {
            tableObjectBuilder = generateIdTable(table, idColumn)
        } else {
            val tableObjectName = getObjectNameForTable(table)
            val tableName = getTableName(table)
            tableObjectBuilder = TypeSpec.objectBuilder(tableObjectName)

            val superclass = org.jetbrains.exposed.sql.Table::class
            tableObjectBuilder.superclass(superclass)
            tableObjectBuilder.addSuperclassConstructorParameter(
                    "%S",
                    tableName
            )

            addColumns(table, tableObjectBuilder)
        }

        val tableObject = tableObjectBuilder.build()
        for (column in table.columns) {
            columnsToTables[column.fullName] = tableObject
        }

        return tableObject
    }


    fun generateExposedTables(databaseName: String): FileSpec {
        val fileSpec = FileSpec.builder("", "${databaseName.toCamelCase(true)}.kt")
        tables.forEach { fileSpec.addType(generateExposedTable(it)) }

        return fileSpec.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger("MetadataGetterLogger")

        private val numericArgumentsPattern = Pattern.compile("\\((([0-9])+([, ]*[0-9])*)\\)")

        private val exposedPackageName = org.jetbrains.exposed.sql.Table::class.java.packageName
        private val exposedDateTimePackageName = JavaLocalDateColumnType::class.java.packageName
    }
}