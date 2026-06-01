package com.flashcard.modules.study

import kotlinx.serialization.Serializable

@Serializable
data class StudySession(
    val id: Int,
    val userId: String,
    val packageId: Int,
    val startedAt: String,
    val finishedAt: String? = null
)

@Serializable
data class CardReview(
    val id: Int,
    val userId: String,
    val cardId: Int,
    val sessionId: Int,
    val quality: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val nextReview: String,
    val reviewedAt: String
)

// lo que manda el frontend al evaluar una tarjeta
@Serializable
data class ReviewRequest(
    val cardId: Int,
    val quality: Int  // 1=Dificil, 2=Casi, 3=Bien, 4=Facil
)

// resumen al terminar la sesión
@Serializable
data class SessionSummary(
    val sessionId: Int,
    val totalCards: Int,
    val difficult: Int,
    val almost: Int,
    val good: Int,
    val easy: Int,
    val durationMinutes: Int
)