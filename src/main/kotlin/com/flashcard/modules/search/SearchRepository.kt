package com.flashcard.modules.search

import com.flashcard.core.database.PackagesTable
import com.flashcard.core.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object SearchRepository {

    private fun rowToResult(row: ResultRow) = SearchResult(
        id          = row[PackagesTable.id],
        name        = row[PackagesTable.name],
        description = row[PackagesTable.description],
        category    = row[PackagesTable.category],
        cardCount   = row[PackagesTable.cardCount],
        authorName  = row[UsersTable.name]
    )

    // busca paquetes públicos por nombre o categoría
    fun search(query: String?, category: String?): List<SearchResult> = transaction {
        val join = PackagesTable
            .join(UsersTable, JoinType.INNER, PackagesTable.userId, UsersTable.id)

        join.selectAll()
            .where {
                val isPublic   = PackagesTable.isPublic eq true
                val notDeleted = PackagesTable.deletedAt.isNull() as Op<Boolean>

                val queryFilter = if (!query.isNullOrBlank()) {
                    PackagesTable.name.lowerCase() like "%${query.lowercase()}%"
                } else null

                val categoryFilter = if (!category.isNullOrBlank()) {
                    PackagesTable.category.lowerCase() eq category.lowercase()
                } else null

                listOfNotNull(isPublic, notDeleted, queryFilter, categoryFilter)
                    .reduce { acc, op -> acc and op }
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .limit(20)
            .map { rowToResult(it) }
    }

    fun trending(): List<SearchResult> = transaction {
        val join = PackagesTable
            .join(UsersTable, JoinType.INNER, PackagesTable.userId, UsersTable.id)

        join.selectAll()
            .where {PackagesTable.isPublic eq true and (PackagesTable.deletedAt.isNull() as Op<Boolean>)
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .limit(10)
            .map { rowToResult(it) }
    }
}