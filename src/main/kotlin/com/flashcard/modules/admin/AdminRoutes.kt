package com.flashcard.modules.admin

import com.flashcard.core.database.UsersTable
import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.adminRoutes() {

    //consulta como
    // middleware — verifica que el usuario es admin
    suspend fun isAdmin(userId: String): Boolean = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .map { it[UsersTable.isAdmin] }
            .firstOrNull() ?: false
    }
    // GET /admin/metrics
    get("/admin/metrics") {
        val userId = call.getUserId()
        if (userId == null || !isAdmin(userId)) {
            call.respond(HttpStatusCode.Forbidden, MessageResponse("Acceso denegado"))
            return@get
        }
        call.respond(HttpStatusCode.OK, AdminRepository.getMetrics())
    }

    // GET /admin/users
    get("/admin/users") {
        val userId = call.getUserId()
        if (userId == null || !isAdmin(userId)) {
            call.respond(HttpStatusCode.Forbidden, MessageResponse("Acceso denegado"))
            return@get
        }
        call.respond(HttpStatusCode.OK, AdminRepository.getAllUsers())
    }

    // PATCH /admin/users/{id}/suspend
    patch("/admin/users/{id}/suspend") {
        val userId = call.getUserId()
        if (userId == null || !isAdmin(userId)) {
            call.respond(HttpStatusCode.Forbidden, MessageResponse("Acceso denegado"))
            return@patch
        }
        val targetId = call.parameters["id"]
        if (targetId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@patch
        }
        val suspended = AdminRepository.suspendUser(targetId)
        if (!suspended) {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Usuario no encontrado"))
            return@patch
        }
        call.respond(HttpStatusCode.OK, MessageResponse("Usuario suspendido"))
    }

    // PATCH /admin/packages/{id}/unpublish
    patch("/admin/packages/{id}/unpublish") {
        val userId = call.getUserId()
        if (userId == null || !isAdmin(userId)) {
            call.respond(HttpStatusCode.Forbidden, MessageResponse("Acceso denegado"))
            return@patch
        }
        val packageId = call.parameters["id"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("id invalido"))
            return@patch
        }
        val unpublished = AdminRepository.unpublishPackage(packageId)
        if (!unpublished) {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Paquete no encontrado"))
            return@patch
        }
        call.respond(HttpStatusCode.OK, MessageResponse("Paquete despublicado"))
    }
}