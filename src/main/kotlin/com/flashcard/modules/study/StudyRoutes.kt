package com.flashcard.modules.study

import com.flashcard.core.security.requireAuth
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studyRoutes() {

    // POST /study/sessions — iniciar sesión de estudio
    post("/study/sessions") {
        call.requireAuth { userId ->
            val body = call.receive<Map<String, Int>>()
            val packageId = body["packageId"]
            if (packageId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("Falta packageId"))
                return@requireAuth
            }
            val session = StudyService.startSession(userId, packageId)
            call.respond(HttpStatusCode.Created, session)
        }
    }

    // POST /study/sessions/{sessionId}/review — evaluar una tarjeta
    post("/study/sessions/{sessionId}/review") {
        call.requireAuth { userId ->
            val sessionId = call.parameters["sessionId"]?.toIntOrNull()
            if (sessionId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("sessionId invalido"))
                return@requireAuth
            }
            val body = call.receive<ReviewRequest>()
            val review = StudyService.reviewCard(userId, sessionId, body)
            call.respond(HttpStatusCode.OK, review)
        }
    }

    // POST /study/sessions/{sessionId}/finish — terminar sesión
    post("/study/sessions/{sessionId}/finish") {
        call.requireAuth { userId ->
            val sessionId = call.parameters["sessionId"]?.toIntOrNull()
            if (sessionId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("sessionId invalido"))
                return@requireAuth
            }
            val summary = StudyService.finishSession(sessionId)
            call.respond(HttpStatusCode.OK, summary)
        }
    }
}