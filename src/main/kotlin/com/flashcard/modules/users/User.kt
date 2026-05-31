package com.flashcard.modules.users

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String? = null,
    val description: String = "",
    val isPublic: Boolean = true
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null
)