package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CardReviewsTable : Table("card_reviews") {
    val id            = integer("id").autoIncrement()
    val userId        = varchar("user_id", 128)
    val cardId        = integer("card_id")
    val sessionId     = integer("session_id")
    val quality       = integer("quality")
    val easeFactor    = double("ease_factor")
    val intervalDays  = integer("interval_days")
    val nextReview    = datetime("next_review")
    val reviewedAt    = datetime("reviewed_at")
    val clientReviewId = varchar("client_review_id", 36).uniqueIndex().nullable()

    override val primaryKey = PrimaryKey(id)
}