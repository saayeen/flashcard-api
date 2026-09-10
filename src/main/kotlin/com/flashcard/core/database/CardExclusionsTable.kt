package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table

object CardExclusionsTable : Table("card_exclusions") {
    val id             = integer("id").autoIncrement()
    val packageId      = integer("package_id")
    val originalCardId = integer("original_card_id")

    override val primaryKey = PrimaryKey(id)
    init {
        uniqueIndex(packageId, originalCardId)
    }
}