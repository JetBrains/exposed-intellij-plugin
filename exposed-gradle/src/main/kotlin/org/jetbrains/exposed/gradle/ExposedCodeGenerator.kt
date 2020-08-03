package org.jetbrains.exposed.gradle

import com.sksamuel.hoplite.ConfigLoader
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.dao.id.*
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.slf4j.LoggerFactory
import schemacrawler.schema.Column
import schemacrawler.schema.Table
import java.io.File
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import org.jetbrains.exposed.sql.Column as ExposedColumn
import org.jetbrains.exposed.sql.Table as ExposedTable


// TODO support schemas
class ExposedCodeGenerator(private val tables: List<Table>, private val configFileName: String? = null) {
    private val columnToPropertySpec = mutableMapOf<Column, PropertySpec>()
    private val columnToTableSpec = mutableMapOf<Column, TypeSpec>()

    private val columnNameToInitializerBlock = mutableMapOf<String, CodeBlock>()

    inner class TableBuilder(table: Table) {
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
                            MappedIdColumnBuilder(column)
                        } else {
                            MappedColumnBuilder(column)
                        }
                    } else {
                        if (column == idColumn) {
                            IdColumnBuilder(column)
                        } else {
                            ColumnBuilder(column)
                        }
                    }
                    columnBuilder.generateExposedColumnInitializer()
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

        fun build(): TypeSpec {
            val exposedTable = builder.build()
            for (column in tableInfo.table.columns) {
                columnToTableSpec[column] = exposedTable
            }
            return exposedTable
        }
    }

    open inner class ColumnBuilder(column: Column) {
        protected val columnInfo = ColumnInfo(column)
        protected open val builder = PropertySpec.builder(
                getPropertyNameForColumn(column),
                ExposedColumn::class.asTypeName().parameterizedBy(columnInfo.columnKClass!!.asTypeName().copy(nullable = columnInfo.nullable))
        )

        open fun generateExposedColumnInitializer() {
            val initializerBlock = buildCodeBlock {
                generateExposedColumnFunctionCall(columnInfo)
                generateExposedColumnProperties(columnInfo)
            }

            builder.initializer(initializerBlock)
        }

        open fun CodeBlock.Builder.generateExposedColumnFunctionCall(columnInfo: ColumnInfo) {
            val column = columnInfo.column
            val columnKClass = columnInfo.columnKClass!!
            val columnExposedFunction = columnInfo.columnExposedFunction!!
            val columnExposedPackage = columnExposedFunction.javaMethod!!.declaringClass.`package`
            val packageName = if (columnExposedPackage == exposedPackage) {
                ""
            } else {
                columnExposedPackage.name
            }
            val memberName = MemberName(packageName, columnExposedFunction.name)

            if (columnInfo.columnExposedFunction!!.valueParameters.size > 1) {
                val arguments = getColumnFunctionArguments()
                when (columnKClass) {
                    // decimal -> precision, scale
                    BigDecimal::class -> {
                        val precision: String
                        val scale: String
                        if (arguments.isNotEmpty()) {
                            precision = arguments[0]
                            scale = arguments[1]
                        } else {
                            precision = if (column.size >= 0 && column.size <= MaxSize.MAX_DECIMAL_PRECISION) {
                                column.size
                            } else {
                                MaxSize.MAX_DECIMAL_PRECISION
                            }.toString()
                            scale = when {
                                // it's unlikely that this is to ever happen but just to cover the possibility
                                column.decimalDigits > MaxSize.MAX_DECIMAL_SCALE -> MaxSize.MAX_DECIMAL_SCALE
                                column.decimalDigits < 0 -> 0
                                else -> column.decimalDigits
                            }.toString()
                        }
                        add("%M(%S, $precision, $scale)", memberName, columnInfo.columnName)
                    }
                    // char, varchar, binary -> length
                    String::class, ByteArray::class -> {
                        if (columnExposedFunction.name in listOf("char", "varchar", "binary")) {
                            val size = when {
                                arguments.isNotEmpty() -> arguments[0]
                                column.size >= 0 && column.size <= MaxSize.MAX_VARCHAR_SIZE -> column.size.toString()
                                else -> MaxSize.MAX_VARCHAR_SIZE.toString()
                            }
                            add("%M(%S, $size)", memberName, columnInfo.columnName)
                        } else {
                            add("%M(%S)", memberName, columnInfo.columnName)
                        }

                    } else -> add("%M(%S)", memberName, columnInfo.columnName)
                }
            } else {
                add("%M(%S)", memberName, columnInfo.columnName)
            }
        }

        open fun CodeBlock.Builder.generateExposedColumnProperties(columnInfo: ColumnInfo) {
            val column = columnInfo.column
            if (column.isAutoIncremented) {
                // TODO is there a way to access those via reflection?
                add(".%M()", MemberName("", "autoIncrement"))
            }

            if (column.referencedColumn != null) {
                val referencedColumnProperty = columnToPropertySpec[column.referencedColumn]
                        ?: throw ReferencedColumnNotFoundException(
                                "Column ${column.referencedColumn.fullName} referenced by ${column.fullName} not found."
                        )
                val referencedColumnTable = columnToTableSpec[column.referencedColumn]
                if (column.parent == column.referencedColumn.parent) {
                    add(".%M(%N)",
                            MemberName("", "references"),
                            referencedColumnProperty)
                } else {
                    add(".%M(%N.%N)",
                            MemberName("", "references"),
                            referencedColumnTable, // should be not null
                            referencedColumnProperty)
                }
            }

            if (column.isNullable && !column.isPartOfPrimaryKey) {
                add(".%M()", MemberName("", "nullable"))
            }
        }

        private fun getColumnFunctionArguments(): List<String> {
            // for columns like 'varchar(30)' the arguments are contained in the full name
            val columnType = columnInfo.column.columnDataType.fullName
            val arguments = mutableListOf<String>()

            val argumentsStart = columnType.indexOfFirst { it == '(' }
            val argumentsEnd = columnType.indexOfLast { it == ')' }
            if (argumentsStart != -1 && argumentsEnd != -1) {
                val argumentsString = columnType.substring(argumentsStart + 1, argumentsEnd)
                arguments.addAll(argumentsString.split(",").map { it.trim() })
            }

            return arguments
        }

        fun build() = builder.build()
    }

    inner class IdColumnBuilder(column: Column) : ColumnBuilder(column) {
        override val builder = PropertySpec.builder(
                getPropertyNameForColumn(column),
                ExposedColumn::class.asTypeName().parameterizedBy(EntityID::class.asTypeName().parameterizedBy(columnInfo.columnKClass!!.asTypeName())),
                KModifier.OVERRIDE
        )

        override fun CodeBlock.Builder.generateExposedColumnProperties(columnInfo: ColumnInfo) {
            // it can't be nullable
            // it can't be auto-incremented because that's only for int and long and it's handled by respective classes
            // TODO can it reference other columns?
            add(".%M()", MemberName("", "entityId"))
        }
    }

    open inner class MappedColumnBuilder(column: Column) : ColumnBuilder(column) {
        private val columnMapping = columnNameToInitializerBlock[getColumnConfigName(column)]!!
        protected val mappedColumnType = getColumnTypeByFunctionCall(columnMapping.toString())

        private fun getColumnTypeByFunctionCall(functionCall: String) = when (functionCall.takeWhile { it != '(' }) {
            "byte" -> Byte::class
            "short" -> Short::class
            "integer" -> Integer::class
            "long" -> Long::class
            "decimal" -> BigDecimal::class
            "float" -> Float::class
            "double" -> Double::class
            "boolean" -> Boolean::class
            "binary" -> ByteArray::class
            "blob" -> ExposedBlob::class
            "char", "varchar", "text" -> String::class
            "date" -> LocalDate::class
            "datetime" -> LocalDateTime::class
            else -> throw UnparseableExposedCallException("Unable to determine type of expression $functionCall and generate column.")
        }

        override val builder = PropertySpec.builder(
                getPropertyNameForColumn(column),
                ExposedColumn::class.asTypeName().parameterizedBy(mappedColumnType.asTypeName().copy(nullable = columnInfo.nullable))
        )

        override fun CodeBlock.Builder.generateExposedColumnFunctionCall(columnInfo: ColumnInfo) {
            add(columnMapping)
        }
    }

    inner class MappedIdColumnBuilder(column: Column) : MappedColumnBuilder(column) {
        override val builder = PropertySpec.builder(
                getPropertyNameForColumn(column),
                ExposedColumn::class.asTypeName().parameterizedBy(EntityID::class.asTypeName().parameterizedBy(mappedColumnType.asTypeName())),
                KModifier.OVERRIDE
        )
    }

    data class TableInfo(val table: Table) {
        val primaryKeyColumns: List<Column> = if (table.hasPrimaryKey()) table.primaryKey.columns else emptyList()
        val idColumn: Column? = if (primaryKeyColumns.size == 1) {
            val column = primaryKeyColumns[0]
            val columnInfo = ColumnInfo(column)
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
            ExposedTable::class
        } else {
            when (ColumnInfo(idColumn).columnKClass) {
                Int::class -> IntIdTable::class
                Long::class -> LongIdTable::class
                UUID::class -> UUIDTable::class
                else -> IdTable::class
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    data class ColumnInfo(val column: Column) {
        val columnName = getColumnName(column)
        var columnKClass: KClass<*>? = null
            private set

        var columnExposedFunction: KFunction<*>? = null
            private set

        var nullable: Boolean = column.isNullable && !column.isPartOfPrimaryKey

        init {
            val exposedChar: KFunction<ExposedColumn<String>> = ExposedTable::class.memberFunctions.find {
                func -> func.name == "char" && func.parameters.any { p -> p.name == "length" }
            } as KFunction<ExposedColumn<String>>
            val exposedBinary: KFunction<ExposedColumn<ByteArray>> = ExposedTable::class.memberFunctions.find {
                func -> func.name == "binary" && func.parameters.any { p -> p.name == "length" }
            } as KFunction<ExposedColumn<ByteArray>>

            fun <T : Any> initializeColumnParameters(columnClass: KClass<T>, columnFunction: KFunction<ExposedColumn<T>>) {
                columnKClass = columnClass
                columnExposedFunction = columnFunction
            }

            fun initializeInteger() {
                when (column.columnDataType.name.toLowerCase()) {
                    "tinyint" -> initializeColumnParameters(Byte::class, ExposedTable::byte)
                    "smallint", "int2" -> initializeColumnParameters(Short::class, ExposedTable::short)
                    "int8" -> initializeColumnParameters(Long::class, ExposedTable::long)
                    else -> initializeColumnParameters(Int::class, ExposedTable::integer)
                }
            }

            fun initializeDouble() {
                val name = column.columnDataType.name.toLowerCase()
                if (name.contains("decimal") || name.contains("numeric")) {
                    initializeColumnParameters(
                            BigDecimal::class,
                            ExposedTable::decimal
                    )
                } else {
                    initializeColumnParameters(Double::class, ExposedTable::double)
                }
            }

            fun initializeString() {
                val name = column.columnDataType.name.toLowerCase()
                when {
                    name.contains("varchar") || name.contains("varying") ->
                        initializeColumnParameters(String::class, ExposedTable::varchar)
                    name.contains("char") ->
                        initializeColumnParameters(String::class, exposedChar)
                    name.contains("text") -> initializeColumnParameters(String::class, ExposedTable::text)
                    name.contains("time") ->
                        initializeColumnParameters(LocalDateTime::class, ExposedTable::datetime)
                    name.contains("date") ->
                        initializeColumnParameters(LocalDate::class, ExposedTable::date)
                    name.contains("binary") || name.contains("bytea") ->
                        initializeColumnParameters(ByteArray::class, exposedBinary)
                    // this is what SQLite occasionally uses for single precision floating point numbers
                    name.contains("single") -> initializeColumnParameters(Float::class, ExposedTable::float)
                }
            }

            fun initializeObject() {
                when (column.columnDataType.name.toLowerCase()) {
                    "uuid" -> initializeColumnParameters(UUID::class, ExposedTable::uuid)
                }
            }


            when (column.columnDataType.typeMappedClass) {
                Integer::class.javaObjectType -> initializeInteger()
                Long::class.javaObjectType -> initializeColumnParameters(Long::class, ExposedTable::long)
                BigDecimal::class.javaObjectType -> initializeColumnParameters(BigDecimal::class, ExposedTable::decimal)
                Float::class.javaObjectType -> initializeColumnParameters(Float::class, ExposedTable::float)
                Double::class.javaObjectType -> initializeDouble()
                Boolean::class.javaObjectType -> initializeColumnParameters(Boolean::class, ExposedTable::bool)
                String::class.javaObjectType -> initializeString()
                Clob::class.javaObjectType -> initializeColumnParameters(String::class, ExposedTable::text)
                Blob::class.javaObjectType -> initializeColumnParameters(ExposedBlob::class, ExposedTable::blob)
                UUID::class.javaObjectType -> initializeColumnParameters(UUID::class, ExposedTable::uuid)
                Object::class.javaObjectType -> initializeObject()
                Date::class.javaObjectType, LocalDate::class.javaObjectType ->
                    initializeColumnParameters(LocalDate::class, ExposedTable::date)
                Timestamp::class.javaObjectType, LocalDateTime::class.javaObjectType ->
                    initializeColumnParameters(LocalDateTime::class, ExposedTable::datetime)
                else -> {
                    val name = column.columnDataType.name.toLowerCase()
                    when {
                        name.contains("uuid") -> initializeColumnParameters(UUID::class, ExposedTable::uuid)
                        // can be 'varbinary'
                        name.contains("binary") || name.contains("bytea") -> {
                            initializeColumnParameters(ByteArray::class, exposedBinary)
                        }
                    }
                }
            }
        }
    }


    // returns a TypeSpec used for Exposed Kotlin code generation
    private fun generateExposedTable(table: Table): TypeSpec {
        val builder = TableBuilder(table)

        builder.generateExposedTableDeclaration()
        builder.generateExposedTableColumns()
        builder.generateExposedTablePrimaryKey()

        return builder.build()
    }

    fun generateExposedTables(databaseName: String): List<FileSpec> {
        val config = if (configFileName != null) {
            ConfigLoader().loadConfigOrThrow(files = listOf(File(configFileName)))
        } else {
            ExposedCodeGeneratorConfiguration(generatedFileName = "$databaseName.kt")
        }

        if (config.columnMappings.isNotEmpty()) {
            columnNameToInitializerBlock.putAll(config.columnMappings.mapValues { CodeBlock.of(it.value) })
        }

        return if (config.generateSingleFile) {
            val fileSpec = FileSpec.builder(
                    config.packageName,
                    if (config.generatedFileName.isNullOrBlank()) {
                        "${databaseName.toCamelCase(capitalizeFirst = true)}.kt"
                    } else {
                        config.generatedFileName
                    }
            )
            tables.forEach { fileSpec.addType(generateExposedTable(it)) }

            listOf(fileSpec.build())
        } else {
            val fileSpecs = mutableListOf<FileSpec>()
            for (table in tables) {
                val fileSpec = FileSpec.builder(config.packageName, "${table.fullName.toCamelCase(capitalizeFirst = true)}.kt")
                fileSpec.addType(generateExposedTable(table))
            }

            fileSpecs
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger("MetadataGetterLogger")
        private val exposedPackage = ExposedTable::class.java.`package`
    }
}