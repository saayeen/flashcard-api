package com.flashcard.modules.packages

import com.flashcard.core.database.PackagesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PackageRepository {

    // convierte "historia,chile,paes" → listOf("historia","chile","paes")
    private fun parseTags(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }

    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id               = row[PackagesTable.id],
        userId           = row[PackagesTable.userId],
        name             = row[PackagesTable.name],
        description      = row[PackagesTable.description],
        category         = row[PackagesTable.category],
        cardCount        = row[PackagesTable.cardCount],
        isPublic         = row[PackagesTable.isPublic],
        theme            = row[PackagesTable.theme] ?: "default",
        tags             = parseTags(row[PackagesTable.tags]),
        forkedFromId     = row[PackagesTable.forkedFromId],
        originalAuthorId = row[PackagesTable.originalAuthorId]
    )

    fun findAllPublic(): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.isPublic eq true and PackagesTable.deletedAt.isNull() }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findOwnedByUser(userId: String): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where {
                PackagesTable.userId eq userId and
                        PackagesTable.deletedAt.isNull() and
                        PackagesTable.forkedFromId.isNull()
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findForkedByUser(userId: String): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where {
                PackagesTable.userId eq userId and
                        PackagesTable.deletedAt.isNull() and
                        PackagesTable.forkedFromId.isNotNull()
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findById(id: Int): FlashcardPackage? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq id }
            .map { rowToPackage(it) }
            .singleOrNull()
    }

    fun create(
        userId: String, name: String, description: String,
        category: String, isPublic: Boolean, theme: String,
        tags: List<String> = emptyList()
    ): FlashcardPackage = transaction {
        val newId = PackagesTable.insert {
            it[PackagesTable.userId]      = userId
            it[PackagesTable.name]        = name
            it[PackagesTable.description] = description
            it[PackagesTable.category]    = category
            it[PackagesTable.isPublic]    = isPublic
            it[PackagesTable.theme]       = theme
            it[PackagesTable.tags]        = tags.joinToString(",")
        } get PackagesTable.id
        findById(newId)!!
    }

    fun update(
        id: Int, name: String?, description: String?,
        category: String?, isPublic: Boolean?,
        tags: List<String>? = null
    ): FlashcardPackage? = transaction {
        PackagesTable.update({ PackagesTable.id eq id }) {
            if (name        != null) it[PackagesTable.name]        = name
            if (description != null) it[PackagesTable.description] = description
            if (category    != null) it[PackagesTable.category]    = category
            if (isPublic    != null) it[PackagesTable.isPublic]    = isPublic
            if (tags        != null) it[PackagesTable.tags]        = tags.joinToString(",")
        }
        findById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        PackagesTable.deleteWhere { PackagesTable.id eq id } > 0
    }

    fun getOwnerId(packageId: Int): String? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq packageId }
            .map { it[PackagesTable.userId] }
            .firstOrNull()
    }
}