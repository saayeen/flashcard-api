package com.flashcard.routes

import com.flashcard.models.CreatePackageRequest
import com.flashcard.models.MessageResponse
import com.flashcard.security.requireAuth
import com.flashcard.services.PackageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Esta función registra todas las rutas de paquetes
// equivalente a un router de Express:
// const router = express.Router()
// router.get('/', ...)
// module.exports = router
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
                call.respond(HttpStatusCode.BadRequest, MessageResponse("El id debe ser un número"))
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
}