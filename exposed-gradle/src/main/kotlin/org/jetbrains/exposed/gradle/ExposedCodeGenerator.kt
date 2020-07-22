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

    // column to its property spec
    private val processedColumns: LinkedHashMap<Column, PropertySpec> = LinkedHashMap()
    // column to its parent table spec
    private val columnsToTables: HashMap<Column, TypeSpec> = HashMap()


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

    private data class ColumnDefinition(
            val columnKClass: KClass<*>,
            val columnInitializationBlock: CodeBlock,
            val nullable: Boolean
    )

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
                    MemberName("", "autoIncrement")
            ))
        }

        if (column.referencedColumn != null) {
            val referencedColumnProperty = processedColumns[column.referencedColumn]
                    ?: throw MetadataReferencedColumnNotFoundException(
                            "Column ${column.referencedColumn.fullName} referenced by ${column.fullName} not found."
                    )
            val referencedColumnTable = columnsToTables[column.referencedColumn]
            val blockToAppend = if (column.parent == column.referencedColumn.parent) {
                CodeBlock.of(
                        ".%M(%N)",
                        MemberName("", "references"),
                        referencedColumnProperty
                )
            } else {
                CodeBlock.of(
                        ".%M(%N.%N)",
                        MemberName("", "references"),
                        referencedColumnTable, // should be not null
                        referencedColumnProperty
                )
            }
            columnInitializerBlock = columnInitializerBlock!!.append(blockToAppend)
        }

        // even though some DBs, like SQLite, allow nullable primary keys to support legacy systems,
        // I don't think we should
        if (column.isNullable && !column.isPartOfPrimaryKey) {
            columnInitializerBlock = columnInitializerBlock!!.append(CodeBlock.of(
                    ".%M()",
                    MemberName("", "nullable")
            ))
        }

        return ColumnDefinition(columnType!!, columnInitializerBlock!!, column.isNullable && !column.isPartOfPrimaryKey)
    }

    private fun CodeBlock.append(codeBlock: CodeBlock): CodeBlock {
        return this.toBuilder().add(codeBlock).build()
    }

    private fun generatePropertyForColumn(column: Column): PropertySpec {
        val columnVariableName = getPropertyNameForColumn(column)
        val columnDefinition = generateColumnDefinition(column)

        return PropertySpec.Companion.builder(
                columnVariableName,
                org.jetbrains.exposed.sql.Column::class.asTypeName().parameterizedBy(
                        columnDefinition.columnKClass.asTypeName().copy(nullable = columnDefinition.nullable)
                )
        ).initializer(columnDefinition.columnInitializationBlock).build()
    }

    // TODO extension method?
    private fun addColumns(table: Table, tableObjectBuilder: TypeSpec.Builder, idColumn: Column? = null) {
        for (column in table.columns) {
            if (column == idColumn) {
                continue
            }
            try {
                val columnProperty = generatePropertyForColumn(column)
                tableObjectBuilder.addProperty(columnProperty)
                processedColumns[column] = columnProperty
            } catch (e: MetadataUnsupportedTypeException) {
                // TODO log the stacktrace or not? technically this should be readable by the client, so... not?
                logger.error("Unsupported type", e)
            }
        }
    }

    private fun generateIdTable(table: Table, idColumn: Column, tableObjectBuilder: TypeSpec.Builder){
        val tableName = getTableName(table)

        val idColumnDefinition = generateColumnDefinition(idColumn)
        val columnPropertyBuilder = PropertySpec.builder("id", org.jetbrains.exposed.sql.Column::class.asClassName()
                .parameterizedBy(EntityID::class.parameterizedBy(idColumnDefinition.columnKClass)))

        val superclass = when (idColumn.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> IntIdTable::class
            Long::class.javaObjectType -> LongIdTable::class
            else -> if (idColumn.columnDataType.name.equals("uuid", ignoreCase = true)) UUIDTable::class else IdTable::class
        }

        if (superclass == IdTable::class) {
            tableObjectBuilder.superclass(superclass.parameterizedBy(idColumnDefinition.columnKClass))
            tableObjectBuilder.addSuperclassConstructorParameter(
                    "%S",
                    tableName
            )

            val idProperty = columnPropertyBuilder
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(idColumnDefinition.columnInitializationBlock
                            .append(CodeBlock.of(".%M()", MemberName("", "entityId")))
                    )
                    .build()
            tableObjectBuilder.addProperty(idProperty)
            processedColumns[idColumn] = idProperty

            generatePrimaryKey(listOf(idColumn), tableObjectBuilder)
        } else {
            tableObjectBuilder.superclass(superclass)
            tableObjectBuilder.addSuperclassConstructorParameter(
                    "%S, %S",
                    tableName,
                    getColumnName(idColumn) // to specify the id column name, which might not be "id"
            )
            processedColumns[idColumn] = columnPropertyBuilder.build()
        }

        addColumns(table, tableObjectBuilder, idColumn)
    }

    // TODO extension method?
    private fun generatePrimaryKey(
            primaryKeyColumns: List<Column>,
            tableObjectBuilder: TypeSpec.Builder
    ) {
        val primaryKeys = primaryKeyColumns.map {
            processedColumns[it]?.name
                    ?: throw MetadataReferencedColumnNotFoundException("Primary key column ${it.fullName} not found.")
        }
        val primaryKey = PropertySpec.builder(
                "primaryKey",
                ClassName("", "PrimaryKey"),
                KModifier.OVERRIDE
        )
                .initializer(CodeBlock.of("%M(${primaryKeys.joinToString(", ")})", MemberName("", "PrimaryKey")))
                .build()
        tableObjectBuilder.addProperty(primaryKey)
    }

    private fun generateTableObjectDeclaration(table: Table, tableObjectBuilder: TypeSpec.Builder) {
        val tableName = getTableName(table)
        val superclass = org.jetbrains.exposed.sql.Table::class
        tableObjectBuilder.superclass(superclass)
        tableObjectBuilder.addSuperclassConstructorParameter(
                "%S",
                tableName
        )
    }

    private fun generatePrimaryKeyTable(table: Table, primaryKeyColumns: List<Column>, tableObjectBuilder: TypeSpec.Builder) {
        fun generateTable() {
            generateTableObjectDeclaration(table, tableObjectBuilder)
            addColumns(table, tableObjectBuilder)
            generatePrimaryKey(primaryKeyColumns, tableObjectBuilder)
        }

        if (primaryKeyColumns.size > 1) {
            generateTable()
        } else {
            val idColumn = primaryKeyColumns[0]
            val columnDefinition = generateColumnDefinition(idColumn)
            if ((columnDefinition.columnKClass == Int::class || columnDefinition.columnKClass == Long::class) && !idColumn.isAutoIncremented) {
                generateTable()
            } else {
                generateIdTable(table, idColumn, tableObjectBuilder)
            }
        }
    }

    private fun generateExposedTable(table: Table): TypeSpec {
        val tableObjectName = getObjectNameForTable(table)
        val tableObjectBuilder = TypeSpec.objectBuilder(tableObjectName)

        val primaryKeyColumns = table.columns.filter { it.isPartOfPrimaryKey }
        if (primaryKeyColumns.isNotEmpty()) {
            generatePrimaryKeyTable(table, primaryKeyColumns, tableObjectBuilder)
        } else {
            generateTableObjectDeclaration(table, tableObjectBuilder)
            addColumns(table, tableObjectBuilder)
        }

        val tableObject = tableObjectBuilder.build()
        for (column in table.columns) {
            columnsToTables[column] = tableObject
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