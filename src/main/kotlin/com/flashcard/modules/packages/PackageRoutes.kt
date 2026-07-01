package com.flashcard.modules.packages

import com.flashcard.core.security.requireAuth
import com.flashcard.core.security.getUserId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.packageRoutes() {

    // todos los públicos — para Home/Trending
    get("/packages") {
        call.respond(HttpStatusCode.OK, PackageRepository.findAllPublic())
    }

    get("/packages/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, MessageResponse("id inválido"))
        val pkg = PackageRepository.findById(id)
            ?: return@get call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
        call.respond(HttpStatusCode.OK, pkg)
    }

    post("/packages") {
        call.requireAuth { userId ->
            val body = call.receive<CreatePackageRequest>()
            val nuevo = PackageService.create(body, userId)
            call.respond(HttpStatusCode.Created, nuevo)
        }
    }

    patch("/packages/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@requireAuth call.respond(HttpStatusCode.BadRequest, MessageResponse("id inválido"))
            val body = call.receive<UpdatePackageRequest>()
            try {
                val updated = PackageService.update(id, userId, body)
                    ?: return@requireAuth call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Forbidden, MessageResponse(e.message ?: "Sin permiso"))
            }
        }
    }

    delete("/packages/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@requireAuth call.respond(HttpStatusCode.BadRequest, MessageResponse("id inválido"))
            try {
                if (!PackageService.delete(id, userId))
                    return@requireAuth call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
                call.respond(HttpStatusCode.OK, MessageResponse("Paquete eliminado"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Forbidden, MessageResponse(e.message ?: "Sin permiso"))
            }
        }
    }

    // paquetes originales del usuario autenticado
    get("/users/me/packages") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findOwnedByUser(userId))
        }
    }

    // paquetes copiados (fork) del usuario autenticado
    get("/users/me/packages/forked") {
        call.requireAuth { userId ->
            call.respond(HttpStatusCode.OK, PackageRepository.findForkedByUser(userId))
        }
    }
}