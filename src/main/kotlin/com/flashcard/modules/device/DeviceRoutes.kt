package com.flashcard.device

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.deviceRoutes() {
    authenticate("firebase-auth") {
        post("/devices/register") {
            val userId = call.principal<FirebaseUser>()!!.uid // ajusta al nombre real de tu principal
            val body = call.receive<RegisterTokenRequest>()
            DeviceRepository.registerToken(userId, body.token)
            call.respond(HttpStatusCode.OK)
        }
    }
}