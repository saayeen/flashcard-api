package com.flashcard.modules.users

import com.flashcard.core.security.requireAuth
import com.flashcard.core.security.getUserId
import com.flashcard.modules.collaboration.CollaborationRepository
import com.flashcard.modules.packages.PackageRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {

    post("/auth/login") {
        val body = call.receive<Map<String, String>>()
        val idToken = body["idToken"] ?: return@post call.respond(
            HttpStatusCode.BadRequest, mapOf("message" to "Falta el idToken")
        )
        val user = UserService.loginOrRegister(idToken) ?: return@post call.respond(
            HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido")
        )
        call.respond(HttpStatusCode.OK, user)
    }

    // ── rutas /me — van primero para no ser capturadas por {id} ──
    get("/users/me") {
        call.requireAuth { userId ->
            val user = UserService.getProfile(userId)
                ?: return@requireAuth call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
            call.respond(HttpStatusCode.OK, user)
        }
    }

    patch("/users/me") {
        call.requireAuth { userId ->
            val body = call.receive<UpdateUserRequest>()
            val updated = UserService.updateProfile(userId, body)
                ?: return@requireAuth call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
            call.respond(HttpStatusCode.OK, updated)
        }
    }

    get("/users/me/packages") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findOwnedByUser(userId))
        }
    }

    get("/users/me/packages/forked") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findForkedByUser(userId))
        }
    }

    // ── rutas específicas /{id}/... — antes que /{id} genérico ──

    get("/users/{id}/packages") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val packages = PackageRepository.findOwnedByUser(userId).filter { it.isPublic }
        call.respond(HttpStatusCode.OK, packages)
    }

    get("/users/{id}/followers/count") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        call.respond(HttpStatusCode.OK, mapOf("count" to CollaborationRepository.getFollowersCount(userId)))
    }

    get("/users/{id}/following/count") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        call.respond(HttpStatusCode.OK, mapOf("count" to CollaborationRepository.getFollowingCount(userId)))
    }

    get("/users/{id}/followers") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val followerIds = CollaborationRepository.getFollowers(userId)
        val users = followerIds.mapNotNull { uid ->
            UserRepository.findById(uid)?.let { u ->
                mapOf("id" to u.id, "name" to u.name, "photoUrl" to (u.photoUrl ?: ""))
            }
        }
        call.respond(HttpStatusCode.OK, users)
    }

    get("/users/{id}/following") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val followingIds = CollaborationRepository.getFollowing(userId)
        val users = followingIds.mapNotNull { uid ->
            UserRepository.findById(uid)?.let { u ->
                mapOf("id" to u.id, "name" to u.name, "photoUrl" to (u.photoUrl ?: ""))
            }
        }
        call.respond(HttpStatusCode.OK, users)
    }

    get("/users/{id}/is-following") {
        val currentUserId = call.getUserId()
        if (currentUserId == null) {
            call.respond(HttpStatusCode.OK, mapOf("following" to false))
            return@get
        }
        val targetId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        call.respond(HttpStatusCode.OK, mapOf(
            "following" to CollaborationRepository.isFollowing(currentUserId, targetId)
        ))
    }

    // ── ruta genérica /{id} —
    get("/users/{id}") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val user = UserService.getProfile(userId)
            ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
        call.respond(HttpStatusCode.OK, user)
    }
}