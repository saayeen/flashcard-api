package com.flashcard.modules.stats

import com.flashcard.core.database.CardReviewsTable
import com.flashcard.core.database.CardsTable
import com.flashcard.core.database.StudySessionsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StatsRepository {

    fun getGlobalStats(userId: String): GlobalStats = transaction {
        val reviews = CardReviewsTable.selectAll()
            .where { CardReviewsTable.userId eq userId }
            .toList()

        val sessions = StudySessionsTable.selectAll()
            .where { StudySessionsTable.userId eq userId }
            .count().toInt()

        GlobalStats(
            totalCardsReviewed = reviews.size,
            totalSessions      = sessions,
            currentStreak      = calculateStreak(userId),
            distribution = ReviewDistribution(
                difficult = reviews.count { it[CardReviewsTable.quality] == 1 },
                almost    = reviews.count { it[CardReviewsTable.quality] == 2 },
                good      = reviews.count { it[CardReviewsTable.quality] == 3 },
                easy      = reviews.count { it[CardReviewsTable.quality] == 4 }
            )
        )
    }

    fun getPackageStats(userId: String, packageId: Int): PackageStats = transaction {
        val totalCards = CardsTable.selectAll()
            .where { CardsTable.packageId eq packageId and (CardsTable.deletedAt.isNull()) }
            .count().toInt()

        val reviews = CardReviewsTable.selectAll()
            .where { CardReviewsTable.userId eq userId }
            .toList()

        val nextReview = CardReviewsTable.selectAll()
            .where { CardReviewsTable.userId eq userId }
            .orderBy(CardReviewsTable.nextReview, SortOrder.ASC)
            .limit(1)
            .map { it[CardReviewsTable.nextReview].format(DateTimeFormatter.ISO_LOCAL_DATE) }
            .firstOrNull()

        PackageStats(
            packageId      = packageId,
            totalCards     = totalCards,
            reviewedCards  = reviews.size,
            distribution   = ReviewDistribution(
                difficult = reviews.count { it[CardReviewsTable.quality] == 1 },
                almost    = reviews.count { it[CardReviewsTable.quality] == 2 },
                good      = reviews.count { it[CardReviewsTable.quality] == 3 },
                easy      = reviews.count { it[CardReviewsTable.quality] == 4 }
            ),
            nextReviewDate = nextReview
        )
    }

    fun getWeeklyActivity(userId: String): List<WeeklyActivity> = transaction {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)

        val reviews = CardReviewsTable.selectAll()
            .where {
                CardReviewsTable.userId eq userId and
                        (CardReviewsTable.reviewedAt greaterEq sevenDaysAgo)
            }
            .toList()

        // agrupa por día
        val byDay = reviews.groupBy {
            it[CardReviewsTable.reviewedAt].format(formatter)
        }

        // genera los últimos 7 días aunque no tengan actividad
        (0..6).map { daysAgo ->
            val date = LocalDateTime.now().minusDays(daysAgo.toLong()).format(formatter)
            WeeklyActivity(
                day           = date,
                cardsReviewed = byDay[date]?.size ?: 0
            )
        }.reversed()
    }

    private fun calculateStreak(userId: String): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val sessions = StudySessionsTable.selectAll()
            .where { StudySessionsTable.userId eq userId }
            .orderBy(StudySessionsTable.startedAt, SortOrder.DESC)
            .map { it[StudySessionsTable.startedAt].format(formatter) }
            .distinct()

        if (sessions.isEmpty()) return 0

        var streak = 0
        var currentDate = LocalDateTime.now()

        for (sessionDate in sessions) {
            val expected = currentDate.format(formatter)
            if (sessionDate == expected) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else break
        }

        return streak
    }
}