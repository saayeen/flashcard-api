package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table

// Exposed usa objetos Kotlin para representar tablas
// equivalente a un Model de Sequelize o un Schema de Mongoose
object PackagesTable : Table("packages") {
    val id          = integer("id").autoIncrement()
    val name        = varchar("name", 255)
    val description = text("description")
    val category    = varchar("category", 100)
    val cardCount   = integer("card_count").default(0)
    val isPublic    = bool("is_public").default(true)

    override val primaryKey = PrimaryKey(id)
}