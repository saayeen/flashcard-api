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
    val theme: String = "default"
)

@Serializable
data class CreatePackageRequest(
    val name: String,
    val description: String,
    val category: String,
    val isPublic: Boolean = true,
    val theme: String = "blue"
)

// ← NUEVO
@Serializable
data class UpdatePackageRequest(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val isPublic: Boolean? = null
)

@Serializable
data class MessageResponse(
    val message: String
)