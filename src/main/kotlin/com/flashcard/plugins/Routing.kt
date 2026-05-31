package com.flashcard.plugins

import com.flashcard.modules.cards.cardRoutes
import com.flashcard.modules.packages.packageRoutes
import com.flashcard.modules.users.userRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        userRoutes()
        packageRoutes()
        cardRoutes()
    }
}