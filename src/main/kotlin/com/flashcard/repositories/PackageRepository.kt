package com.flashcard.repositories

import com.flashcard.database.PackagesTable
import com.flashcard.models.FlashcardPackage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


// El repositorio es la capa que habla con la BD
// equivalente a los métodos del Model en Sequelize:
// Package.findAll(), Package.findByPk(), Package.create(), etc.

object PackageRepository {

    // convierte una fila de la BD a un objeto FlashcardPackage
    // equivalente a lo que hace Sequelize automáticamente con sus instancias
    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id          = row[PackagesTable.id],
        name        = row[PackagesTable.name],
        description = row[PackagesTable.description],
        category    = row[PackagesTable.category],
        cardCount   = row[PackagesTable.cardCount],
        isPublic    = row[PackagesTable.isPublic]
    )

    // SELECT * FROM packages
    fun findAll(): List<FlashcardPackage> = transaction {
        PackagesTable.selectAll().map { rowToPackage(it) }
    }

    // SELECT * FROM packages WHERE id = ?
    fun findById(id: Int): FlashcardPackage? = transaction {
        PackagesTable
            .selectAll()
            .where { PackagesTable.id eq id }
            .map { rowToPackage(it) }
            .singleOrNull()
    }

    // INSERT INTO packages (name, description, category, is_public) VALUES (...)
    fun create(name: String, description: String, category: String, isPublic: Boolean): FlashcardPackage = transaction {
        val newId = PackagesTable.insert {
            it[PackagesTable.name]        = name
            it[PackagesTable.description] = description
            it[PackagesTable.category]    = category
            it[PackagesTable.isPublic]    = isPublic
        } get PackagesTable.id

        findById(newId)!!
    }

    // DELETE FROM packages WHERE id = ?
    fun delete(id: Int): Boolean = transaction {
        val deletedRows = PackagesTable.deleteWhere { PackagesTable.id eq id }
        deletedRows > 0   // true si eliminó algo, false si no existía
    }
}