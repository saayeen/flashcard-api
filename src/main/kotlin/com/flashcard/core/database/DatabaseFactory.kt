package com.flashcard.core.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(
        host: String,
        port: Int,
        database: String,
        user: String,
        password: String
    ) {
        // Si Railway entrega DATABASE_URL, la usamos directo
        val rawUrl = System.getenv("DATABASE_URL")
        val (url, resolvedUser, resolvedPassword) = if (rawUrl != null) {
            // Railway: postgresql://user:password@host:port/dbname
            val uri = java.net.URI(rawUrl)
            val (u, p) = uri.userInfo.split(":")
            val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
            Triple(jdbcUrl, u, p)
        } else {
            // Local: usa los parámetros normales
            Triple("jdbc:postgresql://$host:$port/$database", user, password)
        }

        logger.info("Conectando a la base de datos: $url")

        Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = resolvedUser,
            password = resolvedPassword
        )

        transaction {
            SchemaUtils.create(
                PackagesTable, UsersTable, CardsTable,
                StudySessionsTable, CardReviewsTable,
                FollowersTable, ReviewsTable, FoldersTable
            )
            logger.info("Tablas creadas/verificadas correctamente")
        }
    }
}