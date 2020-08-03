package org.jetbrains.exposed.gradle.builders

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.gradle.*
import org.jetbrains.exposed.gradle.info.ColumnInfo
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import schemacrawler.schema.Column
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import org.jetbrains.exposed.sql.Column as ExposedColumn

open class ColumnBuilder(column: Column) {
    protected val columnInfo = ColumnInfo(column)
    protected open val builder: PropertySpec.Builder = generateBuilder()

    private fun generateBuilder(): PropertySpec.Builder {
        val columnKClass = columnInfo.columnKClass
        if (columnKClass == null && columnInfo.columnExposedFunction == null) {
            val column = columnInfo.column
            throw UnsupportedTypeException("Unable to map column ${column.name} of type ${column.columnDataType.fullName} to an Exposed column object.")
        }
        return PropertySpec.builder(
                getPropertyNameForColumn(columnInfo.column),
                ExposedColumn::class.asTypeName().parameterizedBy(columnInfo.columnKClass!!.asTypeName().copy(nullable = columnInfo.nullable))
        )
    }

    open fun generateExposedColumnInitializer(
            columnToPropertySpec: Map<Column, PropertySpec>,
            columnToTableSpec: Map<Column, TypeSpec>
    ) {
        val initializerBlock = buildCodeBlock {
            generateExposedColumnFunctionCall(columnInfo)
            generateExposedColumnProperties(columnInfo, columnToPropertySpec, columnToTableSpec)
        }

        builder.initializer(initializerBlock)
    }

    open fun CodeBlock.Builder.generateExposedColumnFunctionCall(columnInfo: ColumnInfo) {
        val column = columnInfo.column
        val columnKClass = columnInfo.columnKClass!!
        val columnExposedFunction = columnInfo.columnExposedFunction!!
        val columnExposedPackage = columnExposedFunction.javaMethod!!.declaringClass.`package`
        val packageName = if (columnExposedPackage == ExposedCodeGenerator.exposedPackage) {
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

    open fun CodeBlock.Builder.generateExposedColumnProperties(
            columnInfo: ColumnInfo,
            columnToPropertySpec: Map<Column, PropertySpec>,
            columnToTableSpec: Map<Column, TypeSpec>
    ) {
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

class IdColumnBuilder(column: Column) : ColumnBuilder(column) {
    override val builder = PropertySpec.builder(
            getPropertyNameForColumn(column),
            ExposedColumn::class.asTypeName().parameterizedBy(EntityID::class.asTypeName().parameterizedBy(columnInfo.columnKClass!!.asTypeName())),
            KModifier.OVERRIDE
    )

    override fun CodeBlock.Builder.generateExposedColumnProperties(
            columnInfo: ColumnInfo,
            columnToPropertySpec: Map<Column, PropertySpec>,
            columnToTableSpec: Map<Column, TypeSpec>
    ) {
        // it can't be nullable
        // it can't be auto-incremented because that's only for int and long and it's handled by respective classes
        // TODO can it reference other columns?
        add(".%M()", MemberName("", "entityId"))
    }
}

open class MappedColumnBuilder(column: Column, private val columnMapping: String) : ColumnBuilder(column) {
    protected val mappedColumnType = getColumnTypeByFunctionCall(columnMapping)

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
        add(CodeBlock.of(columnMapping))
    }
}

class MappedIdColumnBuilder(column: Column, columnMapping: String) : MappedColumnBuilder(column, columnMapping) {
    override val builder = PropertySpec.builder(
            getPropertyNameForColumn(column),
            ExposedColumn::class.asTypeName().parameterizedBy(EntityID::class.asTypeName().parameterizedBy(mappedColumnType.asTypeName())),
            KModifier.OVERRIDE
    )
}