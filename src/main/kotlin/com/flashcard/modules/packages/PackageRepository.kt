package com.flashcard.modules.packages

import com.flashcard.core.database.PackagesTable
import com.flashcard.core.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import com.flashcard.core.database.ReviewsTable

object PackageRepository {

    // alias para poder unir UsersTable dos veces (dueño actual + autor original)
    private val OriginalAuthorAlias = UsersTable.alias("original_author")

    // convierte "historia,chile,paes" → listOf("historia","chile","paes")
    private fun parseTags(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }

    // query base con el join a users (LEFT JOIN por si el usuario fue eliminado)
    // + join adicional para traer el nombre del autor original (si el paquete es un fork)
    private fun baseQuery() =
        PackagesTable
            .join(UsersTable, JoinType.LEFT, additionalConstraint = { PackagesTable.userId eq UsersTable.id })
            .join(OriginalAuthorAlias, JoinType.LEFT, additionalConstraint = { PackagesTable.originalAuthorId eq OriginalAuthorAlias[UsersTable.id] })
            .selectAll()

    private fun rowToPackage(row: ResultRow) = FlashcardPackage(
        id                 = row[PackagesTable.id],
        userId             = row[PackagesTable.userId],
        userName           = row.getOrNull(UsersTable.name) ?: "Usuario eliminado",
        userPhotoUrl       = row.getOrNull(UsersTable.photoUrl),
        name               = row[PackagesTable.name],
        description        = row[PackagesTable.description],
        category           = row[PackagesTable.category],
        cardCount          = row[PackagesTable.cardCount],
        isPublic           = row[PackagesTable.isPublic],
        theme              = row[PackagesTable.theme] ?: "default",
        tags               = parseTags(row[PackagesTable.tags]),
        forkedFromId       = row[PackagesTable.forkedFromId],
        originalAuthorId   = row[PackagesTable.originalAuthorId],
        originalAuthorName = row.getOrNull(OriginalAuthorAlias[UsersTable.name]),
        avgRating          = getAvgRating(row[PackagesTable.id])
    )

    fun findAllPublic(): List<FlashcardPackage> = transaction {
        baseQuery()
            .where { PackagesTable.isPublic eq true and PackagesTable.deletedAt.isNull() }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findOwnedByUser(userId: String): List<FlashcardPackage> = transaction {
        baseQuery()
            .where {
                PackagesTable.userId eq userId and
                        PackagesTable.deletedAt.isNull() and
                        PackagesTable.forkedFromId.isNull()
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findForkedByUser(userId: String): List<FlashcardPackage> = transaction {
        baseQuery()
            .where {
                PackagesTable.userId eq userId and
                        PackagesTable.deletedAt.isNull() and
                        PackagesTable.forkedFromId.isNotNull()
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToPackage(it) }
    }

    fun findById(id: Int): FlashcardPackage? = transaction {
        baseQuery()
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

    private fun getAvgRating(packageId: Int): Double? {
        val ratings = ReviewsTable
            .select(ReviewsTable.rating)
            .where { ReviewsTable.packageId eq packageId }
            .map { it[ReviewsTable.rating] }
        return if (ratings.isEmpty()) null else ratings.average()
    }
}