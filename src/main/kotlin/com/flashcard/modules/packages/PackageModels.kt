package com.flashcard.modules.packages

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardPackage(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val cardCount: Int = 0,
    val isPublic: Boolean = true,
    val theme: String = "blue"
)

@Serializable
data class CreatePackageRequest(
    val name: String,
    val description: String,
    val category: String,
    val isPublic: Boolean = true,
    val theme: String = "blue"
)

@Serializable
data class MessageResponse(
    val message: String
)