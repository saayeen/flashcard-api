package com.flashcard.modules.collaboration

import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.collaborationRoutes() {

    // POST /packages/{id}/fork — copiar paquete
    post("/packages/{id}/fork") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val packageId = call.parameters["id"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@post
        }
        val forked = CollaborationService.forkPackage(packageId, userId)
        call.respond(HttpStatusCode.Created, forked)
    }

    // POST /users/{id}/follow — seguir o dejar de seguir
    post("/users/{id}/follow") {
        val followerId = call.getUserId()
        if (followerId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val followingId = call.parameters["id"]
        if (followingId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@post
        }
        val result = CollaborationService.follow(followerId, followingId)
        call.respond(HttpStatusCode.OK, result)
    }

    // GET /packages/{id}/reviews — ver reseñas
    get("/packages/{id}/reviews") {
        val packageId = call.parameters["id"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@get
        }
        call.respond(HttpStatusCode.OK, CollaborationService.getReviews(packageId))
    }

    // POST /packages/{id}/reviews — crear reseña
    post("/packages/{id}/reviews") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val packageId = call.parameters["id"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@post
        }
        val body = call.receive<CreateReviewRequest>()
        val review = CollaborationService.createReview(userId, packageId, body)
        call.respond(HttpStatusCode.Created, review)
    }
}