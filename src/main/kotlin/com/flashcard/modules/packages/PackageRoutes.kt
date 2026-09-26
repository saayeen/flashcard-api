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

    // ── Folders ──────────────────────────────────────────────

    get("/folders") {
        call.requireAuth { userId ->
            val folders = PackageService.getAllFolders(userId)
            call.respond(HttpStatusCode.OK, folders)
        }
    }

    post("/folders") {
        call.requireAuth { userId ->
            val body = call.receive<CreateFolderRequest>()
            val folder = PackageService.createFolder(userId, body.name, body.color)
            call.respond(HttpStatusCode.Created, folder)
        }
    }

    patch("/folders/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("El id debe ser un numero"))
                return@requireAuth
            }
            val body = call.receive<UpdateFolderRequest>()
            val updated = PackageService.updateFolder(id, userId, body.name, body.color)
            call.respond(HttpStatusCode.OK, updated)
        }
    }

    delete("/folders/{id}") {
        call.requireAuth { userId ->
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("El id debe ser un numero"))
                return@requireAuth
            }
            val eliminado = PackageService.deleteFolder(id, userId)
            if (!eliminado) {
                call.respond(HttpStatusCode.NotFound, MessageResponse("Carpeta no encontrada"))
                return@requireAuth
            }
            call.respond(HttpStatusCode.OK, MessageResponse("Carpeta eliminada"))
        }
    }

    post("/folders/{id}/packages/{packageId}") {
        call.requireAuth { userId ->
            val folderId = call.parameters["id"]?.toIntOrNull()
            val packageId = call.parameters["packageId"]?.toIntOrNull()
            if (folderId == null || packageId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("ids invalidos"))
                return@requireAuth
            }
            PackageService.addPackageToFolder(folderId, packageId, userId)
            call.respond(HttpStatusCode.OK, MessageResponse("Paquete agregado a la carpeta"))
        }
    }

    delete("/folders/{id}/packages/{packageId}") {
        call.requireAuth { userId ->
            val folderId = call.parameters["id"]?.toIntOrNull()
            val packageId = call.parameters["packageId"]?.toIntOrNull()
            if (folderId == null || packageId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("ids invalidos"))
                return@requireAuth
            }
            PackageService.removePackageFromFolder(folderId, packageId, userId)
            call.respond(HttpStatusCode.OK, MessageResponse("Paquete removido de la carpeta"))
        }
    }

    get("/folders/{id}/packages") {
        call.requireAuth { userId ->
            val folderId = call.parameters["id"]?.toIntOrNull()
            if (folderId == null) {
                call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
                return@requireAuth
            }
            val packages = PackageService.getPackagesInFolder(folderId, userId)
            call.respond(HttpStatusCode.OK, packages)
        }
    }
}