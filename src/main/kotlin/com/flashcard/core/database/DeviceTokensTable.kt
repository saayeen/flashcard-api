package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object DeviceTokensTable : Table("device_tokens") {
    val id        = integer("id").autoIncrement()
    val userId    = varchar("user_id", 128)
    val token     = varchar("token", 255).uniqueIndex()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}