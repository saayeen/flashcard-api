package com.flashcard.modules.study

import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studyRoutes() {

    get("/study/test") {
        call.respond(HttpStatusCode.OK, mapOf("message" to "study routes funcionando"))
    }

    post("/study/sessions") {
        println(">>> llegó al endpoint POST /study/sessions")
        val userId = call.getUserId()
        println(">>> userId: $userId")
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val body = call.receive<Map<String, Int>>()
        val packageId = body["packageId"]
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("Falta packageId"))
            return@post
        }
        val session = StudyService.startSession(userId, packageId)
        call.respond(HttpStatusCode.Created, session)
    }

    post("/study/sessions/{sessionId}/review") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val sessionId = call.parameters["sessionId"]?.toIntOrNull()
        if (sessionId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("sessionId invalido"))
            return@post
        }
        val body = call.receive<ReviewRequest>()
        val review = StudyService.reviewCard(userId, sessionId, body)
        call.respond(HttpStatusCode.OK, review)
    }

    post("/study/sessions/{sessionId}/finish") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val sessionId = call.parameters["sessionId"]?.toIntOrNull()
        if (sessionId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("sessionId invalido"))
            return@post
        }
        val summary = StudyService.finishSession(sessionId)
        call.respond(HttpStatusCode.OK, summary)
    }

    get("/study/last-session") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@get
        }
        val session = StudyRepository.getLastSession(userId)
        if (session == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("message" to "Sin sesiones"))
            return@get
        }
        call.respond(HttpStatusCode.OK, session)
    }
}