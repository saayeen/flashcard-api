package com.flashcard.modules.collaboration
import com.flashcard.modules.packages.PackageRepository
import com.flashcard.modules.packages.FlashcardPackage

object CollaborationService {

    fun forkPackage(originalId: Int, userId: String): FlashcardPackage {
        val existing = CollaborationRepository.findExistingFork(originalId, userId)
        if (existing != null) {
            throw IllegalStateException("Ya forkeaste este paquete")
        }
        return CollaborationRepository.forkPackage(originalId, userId)
    }

    fun hasForked(originalId: Int, userId: String): Boolean {
        return CollaborationRepository.findExistingFork(originalId, userId) != null
    }

    fun follow(followerId: String, followingId: String): FollowResponse {
        require(followerId != followingId) { "No puedes seguirte a ti mismo" }
        val already = CollaborationRepository.isFollowing(followerId, followingId)
        return if (already) {
            CollaborationRepository.unfollow(followerId, followingId)
            val count = CollaborationRepository.getFollowersCount(followingId)
            FollowResponse(message = "Dejaste de seguir al usuario", following = false, followersCount = count)
        } else {
            CollaborationRepository.follow(followerId, followingId)
            val count = CollaborationRepository.getFollowersCount(followingId)
            FollowResponse(message = "Ahora sigues al usuario", following = true, followersCount = count)
        }
    }

    fun createReview(userId: String, packageId: Int, body: CreateReviewRequest): Review {
        require(body.rating in 1..5) { "El rating debe ser entre 1 y 5" }
        val pkg = PackageRepository.findById(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(userId != pkg.userId) { "No puedes reseñar tu propio paquete" }
        require(userId != pkg.originalAuthorId) { "No puedes reseñar un paquete que creaste originalmente" }
        return CollaborationRepository.createReview(userId, packageId, body.rating, body.comment)
    }

    fun getReviews(packageId: Int): List<Review> {
        return CollaborationRepository.getReviews(packageId)
    }

    fun deleteReview(userId: String, packageId: Int): Boolean {
        return CollaborationRepository.deleteReview(userId, packageId)
    }
}