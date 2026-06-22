package com.flashcard.modules.packages

import com.flashcard.core.database.PackagesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PackageRepository {

    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id          = row[PackagesTable.id],
        name        = row[PackagesTable.name],
        description = row[PackagesTable.description],
        category    = row[PackagesTable.category],
        cardCount   = row[PackagesTable.cardCount],
        isPublic    = row[PackagesTable.isPublic]
    )

    fun findAll(): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll().map { rowToPackage(it) }
    }

    fun findById(id: Int): FlashcardPackage? = transaction {
        PackagesTable.selectAll()
            .where { PackagesTable.id eq id }
            .map { rowToPackage(it) }
            .singleOrNull()
    }

    fun create(userId: String, name: String, description: String, category: String, isPublic: Boolean): FlashcardPackage = transaction {
        val newId = PackagesTable.insert {
            it[PackagesTable.userId]      = userId
            it[PackagesTable.name]        = name
            it[PackagesTable.description] = description
            it[PackagesTable.category]    = category
            it[PackagesTable.isPublic]    = isPublic
        } get PackagesTable.id
        findById(newId)!!
    }

    fun delete(id: Int): Boolean = transaction {
        val deletedRows = PackagesTable.deleteWhere { PackagesTable.id eq id }
        deletedRows > 0
    }
}