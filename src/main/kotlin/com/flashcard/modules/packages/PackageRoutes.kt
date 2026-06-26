package com.flashcard.modules.packages

import com.flashcard.core.security.requireAuth
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.packageRoutes() {

    get("/packages") {
        val packages = PackageService.getAll()
        call.respond(HttpStatusCode.OK, packages)
    }

    get("/packages/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("El id debe ser un número"))
            return@get
        }
        val pkg = PackageService.getById(id)
        if (pkg == null) {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
            return@get
        }
        call.respond(HttpStatusCode.OK, pkg)
    }

    post("/packages") {
        call.requireAuth { userId ->
            val body = call.receive<CreatePackageRequest>()
            val nuevo = PackageService.create(body, userId)
            call.respond(HttpStatusCode.Created, nuevo)
        }
    }

    delete("/packages/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("El id debe ser un numero"))
                return@requireAuth
            }
            val eliminado = PackageService.delete(id, userId)
            if (!eliminado) {
                call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
                return@requireAuth
            }
            call.respond(HttpStatusCode.OK, MessageResponse("Paquete eliminado"))
        }
    }

    get("/users/me/packages") {
        call.requireAuth { _ ->
            val packages = PackageService.getAll()
            call.respond(HttpStatusCode.OK, packages)
        }
    }
}