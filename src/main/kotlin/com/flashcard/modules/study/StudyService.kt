package com.flashcard.modules.study

import com.flashcard.modules.cards.CardRepository
import java.time.LocalDateTime

object StudyService {



    fun startSession(userId: String, packageId: Int): StudySession {
        return StudyRepository.createSession(userId, packageId)
    }

    fun reviewCard(
        userId: String,
        sessionId: Int,
        body: ReviewRequest
    ): CardReview {
        require(body.quality in 1..4) { "La calidad debe ser entre 1 y 4" }

        // obtener el último review de esta tarjeta para este usuario
        val lastReview = StudyRepository.getLastReview(userId, body.cardId)

        val currentEF       = lastReview?.easeFactor   ?: 2.5
        val currentInterval = lastReview?.intervalDays ?: 1
        val repetitions     = if (lastReview == null) 0 else 1

        // aplicar el algoritmo SM-2
        val result = SM2Algorithm.calculate(
            quality      = body.quality,
            easeFactor   = currentEF,
            intervalDays = currentInterval,
            repetitions  = repetitions
        )

        val nextReview = LocalDateTime.now().plusDays(result.nextReviewDays.toLong())

        return StudyRepository.saveReview(
            userId       = userId,
            cardId       = body.cardId,
            sessionId    = sessionId,
            quality      = body.quality,
            easeFactor   = result.easeFactor,
            intervalDays = result.intervalDays,
            nextReview   = nextReview
        )
    }

    fun finishSession(sessionId: Int): SessionSummary {
        val reviews = StudyRepository.getSessionReviews(sessionId)
        val session = StudyRepository.finishSession(sessionId)!!

        return SessionSummary(
            sessionId       = sessionId,
            totalCards      = reviews.size,
            difficult       = reviews.count { it.quality == 1 },
            almost          = reviews.count { it.quality == 2 },
            good            = reviews.count { it.quality == 3 },
            easy            = reviews.count { it.quality == 4 },
            durationMinutes = 0
        )
    }
}