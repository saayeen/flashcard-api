package com.flashcard

import com.flashcard.database.DatabaseFactory
import com.flashcard.security.FirebaseAdmin
import io.ktor.server.application.*

fun Application.configureDatabase() {
    val config = environment.config

    // inicializa Firebase
    FirebaseAdmin.init()

    DatabaseFactory.init(
        host     = config.property("database.host").getString(),
        port     = config.property("database.port").getString().toInt(),
        database = config.property("database.name").getString(),
        user     = config.property("database.user").getString(),
        password = config.property("database.password").getString()
    )
}