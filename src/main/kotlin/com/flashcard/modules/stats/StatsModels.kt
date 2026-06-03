// Usa los datos que ya existen en "card_reviews" y "study_sessions".

import com.flashcard.modules.study.SessionSummary
import kotlinx.serialization.Serializable

@Serializable
data class GlobalStats(
    val totalCardsReviewed: Int,
    val totalSession: Int,
    val currentStreak: Int,
    val distribucion: ReviewDistribution
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
    val reviewed: ReviewDistribution,
    val distribucion: ReviewDistribution,
    val nextReviewDate: String?

)

@Serializable
data class WeeklyActivity(
    val day: String,
    val cardsReviewed: Int
)
