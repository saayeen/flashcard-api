package com.flashcard.modules.admin

import kotlinx.serialization.Serializable

@Serializable
data class PlatformMetrics(
    val totalUsers: Int,
    val totalPackages: Int,
    val totalCards: Int,
    val totalSessions: Int
)