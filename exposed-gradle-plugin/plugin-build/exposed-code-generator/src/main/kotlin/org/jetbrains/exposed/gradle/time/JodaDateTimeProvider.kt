package org.jetbrains.exposed.gradle.time

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.date
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Suppress("UNCHECKED_CAST")
object JodaDateTimeProvider : DateTimeProvider {
	override val dateClass: KClass<LocalDate> = LocalDate::class
	override val dateTimeClass: KClass<LocalDateTime> = LocalDateTime::class

	override fun <S> dateTableFun(): KFunction<Column<S>> {
		return Table::date as KFunction<Column<S>>
	}

	override fun <S> dateTimeTableFun(): KFunction<Column<S>> {
		return Table::datetime as KFunction<Column<S>>
	}

}