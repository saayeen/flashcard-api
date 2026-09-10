package com.flashcard.device

import kotlinx.serialization.Serializable

@Serializable
data class RegisterTokenRequest(
    val token: String
)