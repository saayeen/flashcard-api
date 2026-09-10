package com.flashcard.device

import com.flashcard.core.database.DeviceTokensTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object DeviceRepository {

    // Guarda o actualiza el token de un dispositivo
    fun registerToken(userId: String, token: String) = transaction {
        val now = LocalDateTime.now()

        val existing = DeviceTokensTable.selectAll()
            .where { DeviceTokensTable.token eq token }
            .singleOrNull()

        if (existing != null) {
            DeviceTokensTable.update({ DeviceTokensTable.token eq token }) {
                it[DeviceTokensTable.userId]    = userId
                it[DeviceTokensTable.updatedAt] = now
            }
        } else {
            DeviceTokensTable.insert {
                it[DeviceTokensTable.userId]    = userId
                it[DeviceTokensTable.token]     = token
                it[DeviceTokensTable.createdAt] = now
                it[DeviceTokensTable.updatedAt] = now
            }
        }
    }

    // Trae todos los tokens de un usuario (puede tener más de un dispositivo)
    fun getTokensForUser(userId: String): List<String> = transaction {
        DeviceTokensTable.selectAll()
            .where { DeviceTokensTable.userId eq userId }
            .map { it[DeviceTokensTable.token] }
    }

    // Borra un token que Firebase ya reportó como inválido (app desinstalada, etc.)
    fun deleteToken(token: String) = transaction {
        DeviceTokensTable.deleteWhere { DeviceTokensTable.token eq token }
    }
}