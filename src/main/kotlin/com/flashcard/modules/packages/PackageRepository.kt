package com.flashcard.modules.packages

import com.flashcard.core.database.PackagesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object PackageRepository {

    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id          = row[PackagesTable.id],
        userId      = row[PackagesTable.userId],
        type        = row[PackagesTable.type],
        name        = row[PackagesTable.name],
        description = row[PackagesTable.description],
        category    = row[PackagesTable.category],
        cardCount   = row[PackagesTable.cardCount],
        isPublic    = row[PackagesTable.isPublic],
        color       = row[PackagesTable.color]
    )

    fun getOwnerId(packageId: Int): String? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq packageId }
            .map { it[PackagesTable.userId] }
            .singleOrNull()
    }

    fun getType(packageId: Int): String? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq packageId }
            .map { it[PackagesTable.type] }
            .singleOrNull()
    }

    fun findAll(): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.type eq "deck" and PackagesTable.deletedAt.isNull() }
            .map { rowToPackage(it) }
    }

    fun findAllFoldersByUser(userId: String): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where {
                PackagesTable.type eq "folder" and
                        (PackagesTable.userId eq userId) and
                        PackagesTable.deletedAt.isNull()
            }
            .map { rowToPackage(it) }
    }

    fun findById(id: Int): FlashcardPackage? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq id and PackagesTable.deletedAt.isNull() }
            .map { rowToPackage(it) }
            .singleOrNull()
    }

    fun create(
        userId: String,
        type: String = "deck",
        name: String,
        description: String? = null,
        category: String? = null,
        isPublic: Boolean = true,
        color: String? = null
    ): FlashcardPackage = transaction {
        val newId = PackagesTable.insert {
            it[PackagesTable.userId]      = userId
            it[PackagesTable.type]        = type
            it[PackagesTable.name]        = name
            it[PackagesTable.description] = description
            it[PackagesTable.category]    = category
            it[PackagesTable.isPublic]    = isPublic
            it[PackagesTable.color]       = color
        } get PackagesTable.id
        findById(newId)!!
    }

    fun update(id: Int, name: String?, color: String?): FlashcardPackage? = transaction {
        PackagesTable.update({ PackagesTable.id eq id }) {
            if (name != null) it[PackagesTable.name] = name
            if (color != null) it[PackagesTable.color] = color
        }
        findById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        val updated = PackagesTable.update({ PackagesTable.id eq id }) {
            it[PackagesTable.deletedAt] = LocalDateTime.now()
        }
        updated > 0
    }
}