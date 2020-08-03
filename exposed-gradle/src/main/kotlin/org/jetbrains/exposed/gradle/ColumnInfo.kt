package org.jetbrains.exposed.gradle

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
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
        val exposedChar: KFunction<org.jetbrains.exposed.sql.Column<String>> = Table::class.memberFunctions.find {
            func -> func.name == "char" && func.parameters.any { p -> p.name == "length" }
        } as KFunction<org.jetbrains.exposed.sql.Column<String>>
        val exposedBinary: KFunction<org.jetbrains.exposed.sql.Column<ByteArray>> = Table::class.memberFunctions.find {
            func -> func.name == "binary" && func.parameters.any { p -> p.name == "length" }
        } as KFunction<org.jetbrains.exposed.sql.Column<ByteArray>>

        fun <T : Any> initializeColumnParameters(columnClass: KClass<T>, columnFunction: KFunction<org.jetbrains.exposed.sql.Column<T>>) {
            columnKClass = columnClass
            columnExposedFunction = columnFunction
        }

        fun initializeInteger() {
            when (column.columnDataType.name.toLowerCase()) {
                "tinyint" -> initializeColumnParameters(Byte::class, Table::byte)
                "smallint", "int2" -> initializeColumnParameters(Short::class, Table::short)
                "int8" -> initializeColumnParameters(Long::class, Table::long)
                else -> initializeColumnParameters(Int::class, Table::integer)
            }
        }

        fun initializeDouble() {
            val name = column.columnDataType.name.toLowerCase()
            if (name.contains("decimal") || name.contains("numeric")) {
                initializeColumnParameters(
                        BigDecimal::class,
                        Table::decimal
                )
            } else {
                initializeColumnParameters(Double::class, Table::double)
            }
        }

        fun initializeString() {
            val name = column.columnDataType.name.toLowerCase()
            when {
                name.contains("varchar") || name.contains("varying") ->
                    initializeColumnParameters(String::class, Table::varchar)
                name.contains("char") ->
                    initializeColumnParameters(String::class, exposedChar)
                name.contains("text") -> initializeColumnParameters(String::class, Table::text)
                name.contains("time") ->
                    initializeColumnParameters(LocalDateTime::class, Table::datetime)
                name.contains("date") ->
                    initializeColumnParameters(LocalDate::class, Table::date)
                name.contains("binary") || name.contains("bytea") ->
                    initializeColumnParameters(ByteArray::class, exposedBinary)
                // this is what SQLite occasionally uses for single precision floating point numbers
                name.contains("single") -> initializeColumnParameters(Float::class, Table::float)
            }
        }

        fun initializeObject() {
            when (column.columnDataType.name.toLowerCase()) {
                "uuid" -> initializeColumnParameters(UUID::class, Table::uuid)
            }
        }


        when (column.columnDataType.typeMappedClass) {
            Integer::class.javaObjectType -> initializeInteger()
            Long::class.javaObjectType -> initializeColumnParameters(Long::class, Table::long)
            BigDecimal::class.javaObjectType -> initializeColumnParameters(BigDecimal::class, Table::decimal)
            Float::class.javaObjectType -> initializeColumnParameters(Float::class, Table::float)
            Double::class.javaObjectType -> initializeDouble()
            Boolean::class.javaObjectType -> initializeColumnParameters(Boolean::class, Table::bool)
            String::class.javaObjectType -> initializeString()
            Clob::class.javaObjectType -> initializeColumnParameters(String::class, Table::text)
            Blob::class.javaObjectType -> initializeColumnParameters(ExposedBlob::class, Table::blob)
            UUID::class.javaObjectType -> initializeColumnParameters(UUID::class, Table::uuid)
            Object::class.javaObjectType -> initializeObject()
            Date::class.javaObjectType, LocalDate::class.javaObjectType ->
                initializeColumnParameters(LocalDate::class, Table::date)
            Timestamp::class.javaObjectType, LocalDateTime::class.javaObjectType ->
                initializeColumnParameters(LocalDateTime::class, Table::datetime)
            else -> {
                val name = column.columnDataType.name.toLowerCase()
                when {
                    name.contains("uuid") -> initializeColumnParameters(UUID::class, Table::uuid)
                    // can be 'varbinary'
                    name.contains("binary") || name.contains("bytea") -> {
                        initializeColumnParameters(ByteArray::class, exposedBinary)
                    }
                }
            }
        }
    }
}