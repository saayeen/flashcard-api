// Usa los datos que ya existen en "card_reviews" y "study_sessions".

package com.flashcard.modules.stats

import kotlinx.serialization.Serializable

@Serializable
data class GlobalStats(
    val totalCardsReviewed: Int,
    val totalSessions: Int,
    val currentStreak: Int,
    val distribution: ReviewDistribution
)

@Serializable
data class ReviewDistribution(
    val difficult: Int,
    val almost: Int,
    val good: Int,
    val easy: Int
)

@Serializable
data class PackageStats(
    val packageId: Int,
    val totalCards: Int,
    val reviewedCards: Int,
    val distribution: ReviewDistribution,
    val nextReviewDate: String?
)

@Serializable
data class WeeklyActivity(
    val day: String,
    val cardsReviewed: Int
)
