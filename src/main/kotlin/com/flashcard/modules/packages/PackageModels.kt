package com.flashcard.modules.packages

import kotlinx.serialization.Serializable


@Serializable
data class FlashcardPackage(
    val id: Int,
    val userId: String,
    val type: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val cardCount: Int = 0,
    val isPublic: Boolean = true,
    val color: String? = null
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

@Serializable
data class CreateFolderRequest(
    val name: String,
    val color: String = "#6366f1"
)

@Serializable
data class UpdateFolderRequest(
    val name: String? = null,
    val color: String? = null
)