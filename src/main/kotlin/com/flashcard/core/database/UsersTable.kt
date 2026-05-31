package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id          = varchar("id", 128)
    val email       = varchar("email", 255)
    val name        = varchar("name", 255) //por mientras no se usa este
    val photoUrl    = text("photo_url").nullable()
    val description = text("description").default("")
    val isPublic    = bool("is_public").default(true)

    override val primaryKey = PrimaryKey(id)
}