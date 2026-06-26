package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

// Exposed usa objetos Kotlin para representar tablas
// equivalente a un Model de Sequelize o un Schema de Mongoose
object PackagesTable : Table("packages") {
    val id               = integer("id").autoIncrement()
    val userId           = varchar("user_id", 128)
    val name             = varchar("name", 255)
    val description      = text("description")
    val theme = varchar("theme", 50).default("blue")
    val category         = varchar("category", 100)
    val cardCount        = integer("card_count").default(0)
    val isPublic         = bool("is_public").default(true)
    val deletedAt        = datetime("deleted_at").nullable()
    val forkedFromId     = integer("forked_from_id").nullable()
    val originalAuthorId = varchar("original_author_id", 128).nullable()

    override val primaryKey = PrimaryKey(id)
}