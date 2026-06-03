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
        val url = "jdbc:postgresql://$host:$port/$database"

        logger.info("Conectando a la base de datos: $url")

        Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )

        transaction {
            SchemaUtils.create(PackagesTable, UsersTable, CardsTable, StudySessionsTable, CardReviewsTable,
                FolderPackagesTable, FollowersTable, ReviewsTable)
            logger.info("Tablas creadas/verificadas correctamente")
        }
    }
}