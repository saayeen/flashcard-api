package com.flashcard.modules.cards

import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: Int,
    val packageId: Int,
    val question: String,
    val answer: String
)

@Serializable
data class CreateCardRequest(
    val question: String,
    val answer: String
)

@Serializable
data class UpdateCardRequest(
    val question: String? = null,
    val answer: String? = null
)