package org.jetbrains.exposed.gradle.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Suppress("UNCHECKED_CAST")
object KotlinDateTimeProvider : DateTimeProvider {
	override val dateClass: KClass<LocalDate> = LocalDate::class
	override val dateTimeClass: KClass<LocalDateTime> = LocalDateTime::class

	override fun <S> dateTableFun(): KFunction<Column<S>> {
		return Table::date as KFunction<Column<S>>
	}

	override fun <S> dateTimeTableFun(): KFunction<Column<S>> {
		return Table::datetime as KFunction<Column<S>>
	}

}