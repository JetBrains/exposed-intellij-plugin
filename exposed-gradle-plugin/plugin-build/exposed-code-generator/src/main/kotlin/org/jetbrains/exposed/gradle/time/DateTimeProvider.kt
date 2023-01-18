package org.jetbrains.exposed.gradle.time

import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Provides the required date mapping classes for a given date-time implementation
 */
interface DateTimeProvider {
	val dateClass: KClass<*>
	val dateTimeClass: KClass<*>
	fun <S> dateTableFun(): KFunction<Column<S>>
	fun <S> dateTimeTableFun(): KFunction<Column<S>>
}

fun getDateTimeProviderFromConfig(name: String?) = when (name) {
	"java-time" -> JavaDateTimeProvider
	"kotlin-datetime" -> KotlinDateTimeProvider
	else -> JavaDateTimeProvider
}