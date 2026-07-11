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
        val jdbcUrl = "jdbc:postgresql://$host:$port/$database"
        logger.info("Conectando a la base de datos: $jdbcUrl")

        Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )

        crearTablas()
    }

    fun initFromUrl(databaseUrl: String) {
        val uri = java.net.URI(databaseUrl)
        val (user, password) = uri.userInfo.split(":")
        val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
        logger.info("Conectando a la base de datos: $jdbcUrl")

        Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )

        crearTablas()
    }

    private fun crearTablas() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                PackagesTable, UsersTable, CardsTable,
                StudySessionsTable, CardReviewsTable,
                FollowersTable, ReviewsTable, FoldersTable,
                FolderPackagesTable
            )
            logger.info("Tablas creadas/verificadas correctamente")
        }
    }
}