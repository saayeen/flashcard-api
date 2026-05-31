package com.flashcard

import com.flashcard.routes.packageRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        packageRoutes()
        // más adelante agregarás:
        // cardRoutes()
        // userRoutes()
        // studyRoutes()
    }
}