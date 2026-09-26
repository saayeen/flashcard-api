package com.flashcard.modules.collaboration

import com.flashcard.modules.packages.FlashcardPackage
import com.flashcard.modules.packages.PackageRepository

object CollaborationService {

    fun forkPackage(originalId: Int, userId: String): FlashcardPackage {
        val original = PackageRepository.findById(originalId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(original.type != "folder") { "Las carpetas no se pueden forkear" }

        return CollaborationRepository.forkPackage(originalId, userId)
    }

    fun follow(followerId: String, followingId: String): FollowResponse {
        require(followerId != followingId) { "No puedes seguirte a ti mismo" }
        val already = CollaborationRepository.isFollowing(followerId, followingId)
        return if (already) {
            CollaborationRepository.unfollow(followerId, followingId)
            FollowResponse("Dejaste de seguir al usuario", false)
        } else {
            CollaborationRepository.follow(followerId, followingId)
            FollowResponse("Ahora sigues al usuario", true)
        }
    }

    fun createReview(userId: String, packageId: Int, body: CreateReviewRequest): Review {
        require(body.rating in 1..5) { "El rating debe ser entre 1 y 5" }

        val target = PackageRepository.findById(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(target.type != "folder") { "Las carpetas no se pueden reseñar" }   // 👈 nuevo

        return CollaborationRepository.createReview(userId, packageId, body.rating, body.comment)
    }

    fun getReviews(packageId: Int): List<Review> {
        return CollaborationRepository.getReviews(packageId)
    }
}