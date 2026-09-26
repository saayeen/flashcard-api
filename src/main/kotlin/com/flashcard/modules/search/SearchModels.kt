package com.flashcard.modules.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val id: Int,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val cardCount: Int,
    val authorName: String
)