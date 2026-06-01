package com.flashcard.modules.folders

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: Int,
    val userId: String,
    val name: String,
    val color: String = "#6366f1"
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