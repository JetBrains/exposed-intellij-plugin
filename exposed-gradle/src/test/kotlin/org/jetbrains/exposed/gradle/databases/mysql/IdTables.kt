package org.jetbrains.exposed.gradle.databases.mysql

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object Sample1 : IntIdTable("sample_1", "id") {
    val str: Column<String> = text("str")
}

object Sample2 : LongIdTable("sample_2", "id") {
    val str: Column<String> = text("str")
}

object Sample4 : IdTable<String>("sample_4") {
    override val id: Column<EntityID<String>> = varchar("id", 30).entityId()

    override val primaryKey: PrimaryKey? by lazy {
        super.primaryKey ?: PrimaryKey(id)
    }

    val str: Column<String> = text("str")
}

