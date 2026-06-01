package com.flashcard.modules.search

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRoutes() {

    // GET /search?q=ingles&category=idiomas
    get("/search") {
        val query    = call.request.queryParameters["q"]
        val category = call.request.queryParameters["category"]
        val results  = SearchRepository.search(query, category)
        call.respond(HttpStatusCode.OK, results)
    }

    // GET /search/trending — paquetes más recientes públicos
    get("/search/trending") {
        val results = SearchRepository.trending()
        call.respond(HttpStatusCode.OK, results)
    }
}