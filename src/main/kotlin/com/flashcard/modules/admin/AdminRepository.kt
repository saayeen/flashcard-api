package com.flashcard.modules.admin

import com.flashcard.core.database.*
import com.flashcard.modules.packages.FlashcardPackage
import com.flashcard.modules.users.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object AdminRepository {

    fun getMetrics(): PlatformMetrics = transaction {
        PlatformMetrics(
            totalUsers    = UsersTable.selectAll().count().toInt(),
            totalPackages = PackagesTable.selectAll()
                .where { PackagesTable.deletedAt.isNull() as Op<Boolean> }
                .count().toInt(),
            totalCards    = CardsTable.selectAll()
                .where { CardsTable.deletedAt.isNull() as Op<Boolean> }
                .count().toInt(),
            totalSessions = StudySessionsTable.selectAll().count().toInt()
        )
    }

    fun suspendUser(userId: String): Boolean = transaction {
        val updated = UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.isPublic] = false
        }
        updated > 0
    }

    fun unpublishPackage(packageId: Int): Boolean = transaction {
        val updated = PackagesTable.update({ PackagesTable.id eq packageId }) {
            it[PackagesTable.isPublic] = false
            it[PackagesTable.deletedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    fun getAllUsers(): List<User> = transaction {
        UsersTable.selectAll().map { row ->
            User(
                id          = row[UsersTable.id],
                email       = row[UsersTable.email],
                name        = row[UsersTable.name],
                photoUrl    = row[UsersTable.photoUrl],
                description = row[UsersTable.description],
                isPublic    = row[UsersTable.isPublic],
                isAdmin     = row[UsersTable.isAdmin]
            )
        }
    }
}