package com.flashcard.modules.users

import com.flashcard.core.security.requireAuth
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

    // paquetes propios del usuario autenticado
    get("/users/me/packages") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findOwnedByUser(userId))
        }
    }

    // paquetes copiados del usuario autenticado
    get("/users/me/packages/forked") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findForkedByUser(userId))
        }
    }

    // perfil público de otro usuario
    get("/users/{id}") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val user = UserService.getProfile(userId)
            ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("message" to "Usuario no encontrado"))
        call.respond(HttpStatusCode.OK, user)
    }

    // paquetes públicos de un usuario específico
    get("/users/{id}/packages") {
        val userId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("message" to "id invalido"))
        val packages = PackageRepository.findOwnedByUser(userId).filter { it.isPublic }
        call.respond(HttpStatusCode.OK, packages)
    }
}