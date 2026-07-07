package com.flashcard.modules.collaboration

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: Int,
    val userId: String,
    val packageId: Int,
    val rating: Int,
    val comment: String,
    val userName: String = "",
    val userPhotoUrl: String? = null
)
@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val comment: String = ""
)

@Serializable
data class FollowResponse(
    val message: String,
    val following: Boolean
)