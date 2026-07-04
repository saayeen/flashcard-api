package com.flashcard.modules.search

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.searchRoutes() {

    // GET /search?q=ingles&category=idiomas
    get("/search") {
        val query    = call.request.queryParameters["q"]
        val category = call.request.queryParameters["category"]
        call.respond(HttpStatusCode.OK, SearchRepository.search(query, category))
    }

    // GET /search/users?q=juan
    get("/search/users") {
        val query = call.request.queryParameters["q"] ?: ""
        if (query.isBlank()) {
            call.respond(HttpStatusCode.OK, emptyList<UserResult>())
            return@get
        }
        call.respond(HttpStatusCode.OK, SearchRepository.searchUsers(query))
    }

    // GET /search/tags?q=historia
    get("/search/tags") {
        val query = call.request.queryParameters["q"] ?: ""
        if (query.isBlank()) {
            call.respond(HttpStatusCode.OK, emptyList<SearchResult>())
            return@get
        }
        call.respond(HttpStatusCode.OK, SearchRepository.searchByTag(query))
    }

    // GET /search/popular-tags
    get("/search/popular-tags") {
        call.respond(HttpStatusCode.OK, SearchRepository.popularTags())
    }

    // GET /search/trending
    get("/search/trending") {
        call.respond(HttpStatusCode.OK, SearchRepository.trending())
    }
}