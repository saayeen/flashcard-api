package com.flashcard.modules.search

import kotlinx.serialization.Serializable

package com.flashcard.modules.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val cardCount: Int,
    val authorName: String,
    val tags: List<String>,
    val theme: String,
    val avgRating: Double? = null,
    val reviewCount: Int = 0
)

@Serializable
data class UserResult(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val description: String = "",
    val packageCount: Int = 0
)

@Serializable
data class TagResult(
    val tag: String,
    val packageCount: Int
)