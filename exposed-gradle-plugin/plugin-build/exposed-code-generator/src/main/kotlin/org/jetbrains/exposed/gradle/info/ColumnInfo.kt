package org.jetbrains.exposed.gradle.info

import org.jetbrains.exposed.gradle.getColumnName
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.Column as ExposedColumn
import schemacrawler.schema.Column
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

@Suppress("UNCHECKED_CAST")
data class ColumnInfo(val column: Column) {
    val columnName = getColumnName(column)
    var columnKClass: KClass<*>? = null
        private set

    var columnExposedFunction: KFunction<*>? = null
        private set

    var nullable: Boolean = column.isNullable && !column.isPartOfPrimaryKey

    init {
        val exposedChar: KFunction<ExposedColumn<String>> = Table::class.memberFunctions.find {
            func -> func.name == "char" && func.parameters.any { p -> p.name == "length" }
        } as KFunction<ExposedColumn<String>>
        val exposedBinary: KFunction<ExposedColumn<ByteArray>> = Table::class.memberFunctions.find {
            func -> func.name == "binary" && func.parameters.any { p -> p.name == "length" }
        } as KFunction<ExposedColumn<ByteArray>>

        fun <T : Any> initializeColumnParameters(columnClass: KClass<T>, columnFunction: KFunction<ExposedColumn<T>>) {
            columnKClass = columnClass
            columnExposedFunction = columnFunction
        }

        fun initializeInteger() {
            when (column.columnDataType.name.toLowerCase()) {
                "tinyint" -> initializeColumnParameters(Byte::class, getExposedFunction("byte"))
                "smallint", "int2" -> initializeColumnParameters(Short::class, getExposedFunction("short"))
                "int8" -> initializeColumnParameters(Long::class, getExposedFunction("long"))
                else -> initializeColumnParameters(Int::class, getExposedFunction("integer"))
            }
        }

        fun initializeDouble() {
            val name = column.columnDataType.name.toLowerCase()
            if (name.contains("decimal") || name.contains("numeric")) {
                initializeColumnParameters(
                        BigDecimal::class,
                        getExposedFunction("decimal")
                )
            } else {
                initializeColumnParameters(Double::class, getExposedFunction("double"))
            }
        }

        fun initializeString() {
            val name = column.columnDataType.name.toLowerCase()
            when {
                name.contains("varchar") || name.contains("varying") ->
                    initializeColumnParameters(String::class, getExposedFunction("varchar"))
                name.contains("char") ->
                    initializeColumnParameters(String::class, exposedChar)
                name.contains("text") -> initializeColumnParameters(String::class, getExposedFunction("text"))
                name.contains("time") ->
                    initializeColumnParameters(LocalDateTime::class, Table::datetime)
                name.contains("date") ->
                    initializeColumnParameters(LocalDate::class, Table::date)
                name.contains("binary") || name.contains("bytea") ->
                    initializeColumnParameters(ByteArray::class, exposedBinary)
                // this is what SQLite occasionally uses for single precision floating point numbers
                name.contains("single") -> initializeColumnParameters(Float::class, getExposedFunction("float"))
            }
        }

        fun initializeObject() {
            when (column.columnDataType.name.toLowerCase()) {
                "uuid" -> initializeColumnParameters(UUID::class, getExposedFunction("uuid"))
            }
        }


        when (column.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> initializeInteger()
            Long::class.javaObjectType -> initializeColumnParameters(Long::class, getExposedFunction("long"))
            BigDecimal::class.javaObjectType -> initializeColumnParameters(BigDecimal::class, getExposedFunction("decimal"))
            Float::class.javaObjectType -> initializeColumnParameters(Float::class, getExposedFunction("float"))
            Double::class.javaObjectType -> initializeDouble()
            Boolean::class.javaObjectType -> initializeColumnParameters(Boolean::class, getExposedFunction("bool"))
            String::class.javaObjectType -> initializeString()
            Clob::class.javaObjectType -> initializeColumnParameters(String::class, getExposedFunction("text"))
            Blob::class.javaObjectType -> initializeColumnParameters(ExposedBlob::class, getExposedFunction("blob"))
            UUID::class.javaObjectType -> initializeColumnParameters(UUID::class, getExposedFunction("uuid"))
            Object::class.javaObjectType -> initializeObject()
            Date::class.javaObjectType, LocalDate::class.javaObjectType ->
                initializeColumnParameters(LocalDate::class, Table::date)
            Timestamp::class.javaObjectType, LocalDateTime::class.javaObjectType ->
                initializeColumnParameters(LocalDateTime::class, Table::datetime)
            else -> {
                val name = column.columnDataType.name.toLowerCase()
                when {
                    name.contains("uuid") -> initializeColumnParameters(UUID::class, getExposedFunction("uuid"))
                    // can be 'varbinary'
                    name.contains("binary") || name.contains("bytea") -> {
                        initializeColumnParameters(ByteArray::class, exposedBinary)
                    }
                }
            }
        }
    }

    private fun <T> getExposedFunction(name: String) = Table::class.memberFunctions.find{ it.name == name } as KFunction<ExposedColumn<T>>
}