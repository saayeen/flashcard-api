package com.flashcard.modules.stats

import com.flashcard.core.security.getUserId
import com.flashcard.modules.packages.MessageResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statsRoutes() {

    // GET /stats — estadísticas globales del usuario
    get("/stats") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@get
        }
        val stats = StatsRepository.getGlobalStats(userId)
        call.respond(HttpStatusCode.OK, stats)
    }

    // GET /stats/packages/{packageId} — estadísticas por paquete
    get("/stats/packages/{packageId}") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@get
        }
        val packageId = call.parameters["packageId"]?.toIntOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("packageId invalido"))
            return@get
        }
        val stats = StatsRepository.getPackageStats(userId, packageId)
        call.respond(HttpStatusCode.OK, stats)
    }

    // GET /stats/activity — actividad semanal
    get("/stats/activity") {
        val userId = call.getUserId()
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Token invalido o ausente"))
            return@get
        }
        val activity = StatsRepository.getWeeklyActivity(userId)
        call.respond(HttpStatusCode.OK, activity)
    }
}