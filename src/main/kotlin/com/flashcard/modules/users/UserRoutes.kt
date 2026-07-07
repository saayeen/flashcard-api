package com.flashcard.modules.users

import com.flashcard.core.security.requireAuth
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {

    post("/auth/login") {
        val body = call.receive<Map<String, String>>()
        val idToken = body["idToken"]

        if (idToken == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Falta el idToken"))
            return@post
        }

        val user = UserService.loginOrRegister(idToken)

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido"))
            return@post
        }

        call.respond(HttpStatusCode.OK, user)
    }

    get("/users/me") {
        call.requireAuth { userId ->
            val user = UserService.getProfile(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
                return@requireAuth
            }
            call.respond(HttpStatusCode.OK, user)
        }
    }

    patch("/users/me") {
        call.requireAuth { userId ->
            val body = call.receive<UpdateUserRequest>()
            val updated = UserService.updateProfile(userId, body)
            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
                return@requireAuth
            }
            call.respond(HttpStatusCode.OK, updated)
        }
    }

    // GET /users/{id} — perfil público de cualquier usuario
    get("/users/{id}") {
        val userId = call.parameters["id"]
        if (userId == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
            return@get
        }
        val user = UserService.getProfile(userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
            return@get
        }
        call.respond(HttpStatusCode.OK, user)
    }
}