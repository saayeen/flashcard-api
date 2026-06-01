package com.flashcard.modules.folders

import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.folderRoutes() {

    get("/folders") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@get
        }
        call.respond(HttpStatusCode.OK, FolderService.getAll(userId))
    }

    post("/folders") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val body = call.receive<CreateFolderRequest>()
        val folder = FolderService.create(userId, body)
        call.respond(HttpStatusCode.Created, folder)
    }

    patch("/folders/{id}") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@patch
        }
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@patch
        }
        val body = call.receive<UpdateFolderRequest>()
        val updated = FolderService.update(id, body)
        if (updated == null) {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Carpeta no encontrada"))
            return@patch
        }
        call.respond(HttpStatusCode.OK, updated)
    }

    delete("/folders/{id}") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@delete
        }
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@delete
        }
        val eliminado = FolderService.delete(id)
        if (!eliminado) {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Carpeta no encontrada"))
            return@delete
        }
        call.respond(HttpStatusCode.OK, MessageResponse("Carpeta eliminada"))
    }

    post("/folders/{id}/packages/{packageId}") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@post
        }
        val folderId = call.parameters["id"]?.toIntOrNull()
        val packageId = call.parameters["packageId"]?.toIntOrNull()
        if (folderId == null || packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("ids invalidos"))
            return@post
        }
        FolderService.addPackage(folderId, packageId)
        call.respond(HttpStatusCode.OK, MessageResponse("Paquete agregado a la carpeta"))
    }

    delete("/folders/{id}/packages/{packageId}") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@delete
        }
        val folderId = call.parameters["id"]?.toIntOrNull()
        val packageId = call.parameters["packageId"]?.toIntOrNull()
        if (folderId == null || packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("ids invalidos"))
            return@delete
        }
        FolderService.removePackage(folderId, packageId)
        call.respond(HttpStatusCode.OK, MessageResponse("Paquete removido de la carpeta"))
    }
}