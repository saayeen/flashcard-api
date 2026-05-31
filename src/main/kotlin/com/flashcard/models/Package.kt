package com.flashcard.models

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardPackage(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val cardCount: Int = 0,
    val isPublic: Boolean = true
)

@Serializable
data class CreatePackageRequest(
    val name: String,
    val description: String,
    val category: String,
    val isPublic: Boolean = true
)

@Serializable
data class MessageResponse(
    val message: String
)