package com.flashcard.modules.cards

import com.flashcard.core.security.requireAuth
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cardRoutes() {

    // GET /packages/{packageId}/cards — lista tarjetas de un paquete
    get("/packages/{packageId}/cards") {
        val packageId = call.parameters["packageId"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("packageId invalido"))
            return@get
        }
        val cards = CardService.getByPackage(packageId)
        call.respond(HttpStatusCode.OK, cards)
    }

    // POST /packages/{packageId}/cards — crear tarjeta
    post("/packages/{packageId}/cards") {
        call.requireAuth { userId ->
            val packageId = call.parameters["packageId"]?.toIntOrNull()
            if (packageId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("packageId invalido"))
                return@requireAuth
            }
            val body = call.receive<CreateCardRequest>()
            try {
                val card = CardService.create(packageId, userId, body)
                call.respond(HttpStatusCode.Created, card)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Forbidden, MessageResponse(e.message ?: "Sin permiso"))
            }
        }
    }

    patch("/cards/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
                return@requireAuth
            }
            val body = call.receive<UpdateCardRequest>()
            try {
                val updated = CardService.update(id, userId, body)
                if (updated == null) {
                    call.respond(HttpStatusCode.NotFound, MessageResponse("Tarjeta no encontrada"))
                    return@requireAuth
                }
                call.respond(HttpStatusCode.OK, updated)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Forbidden, MessageResponse(e.message ?: "Sin permiso"))
            }
        }
    }

    delete("/cards/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
                return@requireAuth
            }
            try {
                val eliminado = CardService.delete(id, userId)
                if (!eliminado) {
                    call.respond(HttpStatusCode.NotFound, MessageResponse("Tarjeta no encontrada"))
                    return@requireAuth
                }
                call.respond(HttpStatusCode.OK, MessageResponse("Tarjeta eliminada"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Forbidden, MessageResponse(e.message ?: "Sin permiso"))
            }
        }
    }
}