package com.flashcard.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    val frontendUrl = System.getenv("FRONTEND_URL") ?: "localhost:5173"
    val isLocal = frontendUrl.contains("localhost")

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowNonSimpleContentTypes = true

        allowHost("localhost:5173")

        // Origen que usa la app empaquetada con Capacitor (androidScheme: 'http')
        allowHost("localhost", schemes = listOf("http"))

        if (!isLocal) {
            allowHost(frontendUrl, schemes = listOf("https"))
        }
    }
}