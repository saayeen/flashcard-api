package com.flashcard.core.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

suspend fun ApplicationCall.getUserId(): String? {
    val authHeader = request.header("Authorization") ?: return null
    if (!authHeader.startsWith("Bearer ")) return null
    val token = authHeader.removePrefix("Bearer ")
    val decoded = FirebaseAdmin.verifyToken(token) ?: return null
    return decoded.uid
}

suspend fun ApplicationCall.requireAuth(
    block: suspend (userId: String) -> Unit
) {
    val userId = getUserId()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
        return
    }
    block(userId)
}