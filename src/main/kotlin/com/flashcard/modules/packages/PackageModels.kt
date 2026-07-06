package com.flashcard.modules.packages

import kotlinx.serialization.Serializable

@Serializable
data class FlashcardPackage(
    val id: Int,
    val userId: String,
    val name: String,
    val description: String,
    val category: String,
    val cardCount: Int = 0,
    val isPublic: Boolean = true,
    val theme: String = "default",
    val tags: List<String> = emptyList(),
    val forkedFromId: Int? = null,
    val originalAuthorId: String? = null,
    val avgRating: Double? = null  // ← nuevo
)

@Serializable
data class CreatePackageRequest(
    val name: String,
    val description: String,
    val category: String,
    val isPublic: Boolean = true,
    val theme: String = "blue",
    val tags: List<String> = emptyList()        // ← nuevo
)

@Serializable
data class UpdatePackageRequest(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val isPublic: Boolean? = null,
    val tags: List<String>? = null              // ← nuevo
)

@Serializable
data class MessageResponse(val message: String)