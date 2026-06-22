package com.flashcard.modules.study

import com.flashcard.core.database.CardReviewsTable
import com.flashcard.core.database.StudySessionsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StudyRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun rowToSession(row: ResultRow) = StudySession(
        id         = row[StudySessionsTable.id],
        userId     = row[StudySessionsTable.userId],
        packageId  = row[StudySessionsTable.packageId],
        startedAt  = row[StudySessionsTable.startedAt].format(formatter),
        finishedAt = row[StudySessionsTable.finishedAt]?.format(formatter)
    )

    private fun rowToReview(row: ResultRow) = CardReview(
        id           = row[CardReviewsTable.id],
        userId       = row[CardReviewsTable.userId],
        cardId       = row[CardReviewsTable.cardId],
        sessionId    = row[CardReviewsTable.sessionId],
        quality      = row[CardReviewsTable.quality],
        easeFactor   = row[CardReviewsTable.easeFactor],
        intervalDays = row[CardReviewsTable.intervalDays],
        nextReview   = row[CardReviewsTable.nextReview].format(formatter),
        reviewedAt   = row[CardReviewsTable.reviewedAt].format(formatter)
    )

    fun createSession(userId: String, packageId: Int): StudySession = transaction {
        val newId = StudySessionsTable.insert {
            it[StudySessionsTable.userId]    = userId
            it[StudySessionsTable.packageId] = packageId
            it[StudySessionsTable.startedAt] = LocalDateTime.now()
        } get StudySessionsTable.id
        StudySessionsTable.selectAll()
            .where { StudySessionsTable.id eq newId }
            .map { rowToSession(it) }
            .first()
    }

    fun finishSession(sessionId: Int): StudySession? = transaction {
        StudySessionsTable.update({ StudySessionsTable.id eq sessionId }) {
            it[StudySessionsTable.finishedAt] = LocalDateTime.now()
        }
        StudySessionsTable.selectAll()
            .where { StudySessionsTable.id eq sessionId }
            .map { rowToSession(it) }
            .singleOrNull()
    }

    fun getLastReview(userId: String, cardId: Int): CardReview? = transaction {
        CardReviewsTable.selectAll()
            .where { CardReviewsTable.userId eq userId and (CardReviewsTable.cardId eq cardId) }
            .orderBy(CardReviewsTable.reviewedAt, SortOrder.DESC)
            .limit(1)
            .map { rowToReview(it) }
            .singleOrNull()
    }

    fun saveReview(
        userId: String,
        cardId: Int,
        sessionId: Int,
        quality: Int,
        easeFactor: Double,
        intervalDays: Int,
        nextReview: LocalDateTime
    ): CardReview = transaction {
        val newId = CardReviewsTable.insert {
            it[CardReviewsTable.userId]       = userId
            it[CardReviewsTable.cardId]       = cardId
            it[CardReviewsTable.sessionId]    = sessionId
            it[CardReviewsTable.quality]      = quality
            it[CardReviewsTable.easeFactor]   = easeFactor
            it[CardReviewsTable.intervalDays] = intervalDays
            it[CardReviewsTable.nextReview]   = nextReview
            it[CardReviewsTable.reviewedAt]   = LocalDateTime.now()
        } get CardReviewsTable.id
        CardReviewsTable.selectAll()
            .where { CardReviewsTable.id eq newId }
            .map { rowToReview(it) }
            .first()
    }

    fun getSessionReviews(sessionId: Int): List<CardReview> = transaction {
        CardReviewsTable.selectAll()
            .where { CardReviewsTable.sessionId eq sessionId }
            .map { rowToReview(it) }
    }

    fun getDueCards(userId: String, packageId: Int): List<Int> = transaction {
        // devuelve ids de tarjetas que deben revisarse hoy
        CardReviewsTable.selectAll()
            .where {
                CardReviewsTable.userId eq userId and
                        (CardReviewsTable.nextReview lessEq LocalDateTime.now())
            }
            .map { it[CardReviewsTable.cardId] }
    }
}