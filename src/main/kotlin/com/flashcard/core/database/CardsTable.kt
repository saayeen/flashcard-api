package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CardsTable : Table("cards") {
    val id        = integer("id").autoIncrement()
    val packageId = integer("package_id")
    val question  = text("question")
    val answer    = text("answer")
    val deletedAt = datetime("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}