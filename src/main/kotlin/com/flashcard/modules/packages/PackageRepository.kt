package com.flashcard.modules.packages

import com.flashcard.core.database.PackagesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PackageRepository {

    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id          = row[PackagesTable.id],
        userId      = row[PackagesTable.userId],
        name        = row[PackagesTable.name],
        description = row[PackagesTable.description],
        category    = row[PackagesTable.category],
        cardCount   = row[PackagesTable.cardCount],
        isPublic    = row[PackagesTable.isPublic],
        theme       = row[PackagesTable.theme] ?: "default"
    )

    fun findAll(): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.deletedAt.isNull() }
            .map { rowToPackage(it) }
    }

    fun findById(id: Int): FlashcardPackage? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq id }
            .map { rowToPackage(it) }
            .singleOrNull()
    }

    fun create(userId: String, name: String, description: String, category: String, isPublic: Boolean, theme: String): FlashcardPackage = transaction {
        val newId = PackagesTable.insert {
            it[PackagesTable.userId]      = userId
            it[PackagesTable.name]        = name
            it[PackagesTable.description] = description
            it[PackagesTable.category]    = category
            it[PackagesTable.isPublic]    = isPublic
            it[PackagesTable.theme]       = theme
        } get PackagesTable.id
        findById(newId)!!
    }

    // ← NUEVO: actualizar nombre, descripción, categoría, visibilidad
    fun update(
        id: Int,
        name: String?,
        description: String?,
        category: String?,
        isPublic: Boolean?
    ): FlashcardPackage? = transaction {
        PackagesTable.update({ PackagesTable.id eq id }) {
            if (name        != null) it[PackagesTable.name]        = name
            if (description != null) it[PackagesTable.description] = description
            if (category    != null) it[PackagesTable.category]    = category
            if (isPublic    != null) it[PackagesTable.isPublic]    = isPublic
        }
        findById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        val deletedRows = PackagesTable.deleteWhere { PackagesTable.id eq id }
        deletedRows > 0
    }

    fun getOwnerId(packageId: Int): String? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq packageId }
            .map { it[PackagesTable.userId] }
            .firstOrNull()
    }
}