package com.flashcard.modules.collaboration

import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import com.flashcard.modules.users.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UserSummary(
    val id: String,
    val name: String,
    val photoUrl: String? = null
)

fun Route.collaborationRoutes() {

    // POST /packages/{id}/fork
    post("/packages/{id}/fork") {
        val userId = call.getUserId()
            ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
        val packageId = call.parameters["id"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        val forked = CollaborationService.forkPackage(packageId, userId)
        call.respond(HttpStatusCode.Created, forked)
    }

    // POST /users/{id}/follow — toggle seguir/dejar de seguir
    post("/users/{id}/follow") {
        val followerId = call.getUserId()
            ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
        val followingId = call.parameters["id"]
            ?: return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        if (followerId == followingId)
            return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("No puedes seguirte a ti mismo"))

        val nowFollowing = CollaborationRepository.toggleFollow(followerId, followingId)
        val followersCount = CollaborationRepository.getFollowersCount(followingId)
        call.respond(HttpStatusCode.OK, FollowResponse(nowFollowing, followersCount))
    }

    // GET /users/{id}/is-following
    get("/users/{id}/is-following") {
        val currentUserId = call.getUserId()
        if (currentUserId == null) {
            call.respond(HttpStatusCode.OK, mapOf("following" to false))
            return@get
        }
        val targetId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        call.respond(HttpStatusCode.OK, mapOf("following" to false))
    }

    // GET /users/{id}/followers/count
    get("/users/{id}/followers/count") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        call.respond(HttpStatusCode.OK, mapOf("count" to CollaborationRepository.getFollowersCount(userId)))
    }

    // GET /users/{id}/following/count
    get("/users/{id}/following/count") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        call.respond(HttpStatusCode.OK, mapOf("count" to CollaborationRepository.getFollowingCount(userId)))
    }

    // GET /users/{id}/followers — lista de seguidores
    get("/users/{id}/followers") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        val followerIds = CollaborationRepository.getFollowers(userId)
        val users = followerIds.mapNotNull { uid ->
            UserRepository.findById(uid)?.let { u ->
                UserSummary(id = u.id, name = u.name, photoUrl = u.photoUrl)
            }
        }
        call.respond(HttpStatusCode.OK, users)
    }

    // GET /users/{id}/following — lista de usuarios que sigue
    get("/users/{id}/following") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        val followingIds = CollaborationRepository.getFollowing(userId)
        val users = followingIds.mapNotNull { uid ->
            UserRepository.findById(uid)?.let { u ->
                UserSummary(id = u.id, name = u.name, photoUrl = u.photoUrl)
            }
        }
        call.respond(HttpStatusCode.OK, users)
    }

    // GET /packages/{id}/reviews
    get("/packages/{id}/reviews") {
        val packageId = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        call.respond(HttpStatusCode.OK, CollaborationService.getReviews(packageId))
    }

    // POST /packages/{id}/reviews
    post("/packages/{id}/reviews") {
        val userId = call.getUserId()
            ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
        val packageId = call.parameters["id"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
        val body = call.receive<CreateReviewRequest>()
        val review = CollaborationService.createReview(userId, packageId, body)
        call.respond(HttpStatusCode.Created, review)
    }

    // DELETE /packages/{packageId}/reviews
    delete("/packages/{packageId}/reviews") {
        val userId = call.getUserId()
            ?: return@delete call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
        val packageId = call.parameters["packageId"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, MessageResponse("packageId inválido"))
        if (!CollaborationService.deleteReview(userId, packageId))
            return@delete call.respond(HttpStatusCode.NotFound, MessageResponse("Reseña no encontrada"))
        call.respond(HttpStatusCode.OK, MessageResponse("Reseña eliminada"))
    }
}