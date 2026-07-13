package com.flashcard.modules.search

import com.flashcard.core.database.PackagesTable
import com.flashcard.core.database.UsersTable
import com.flashcard.core.database.ReviewsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object SearchRepository {

    private fun parseTags(raw: String): List<String> =
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }

    private fun getAvgRating(packageId: Int): Pair<Double?, Int> {
        val ratings = ReviewsTable
            .select(ReviewsTable.rating)
            .where { ReviewsTable.packageId eq packageId }
            .map { it[ReviewsTable.rating] }
        return if (ratings.isEmpty()) null to 0 else ratings.average() to ratings.size
    }

    private fun rowToResult(row: ResultRow): SearchResult {
        val (avgRating, reviewCount) = getAvgRating(row[PackagesTable.id])
        return SearchResult(
            id          = row[PackagesTable.id],
            name        = row[PackagesTable.name],
            description = row[PackagesTable.description],
            category    = row[PackagesTable.category],
            cardCount   = row[PackagesTable.cardCount],
            authorName  = row[UsersTable.name],
            tags        = parseTags(row[PackagesTable.tags]),
            theme       = row[PackagesTable.theme],
            avgRating   = avgRating,
            reviewCount = reviewCount
        )
    }

    // busca paquetes por nombre o categoría
    fun search(query: String?, category: String?): List<SearchResult> = transaction {
        val join = PackagesTable
            .join(UsersTable, JoinType.INNER, PackagesTable.userId, UsersTable.id)

        join.selectAll()
            .where {
                val isPublic   = PackagesTable.isPublic eq true
                val notDeleted = PackagesTable.deletedAt.isNull() as Op<Boolean>

                val queryFilter = if (!query.isNullOrBlank()) {
                    PackagesTable.name.lowerCase() like "%${query.lowercase()}%" or
                            (PackagesTable.tags.lowerCase() like "%${query.lowercase()}%")
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

    // busca usuarios por nombre
    fun searchUsers(query: String): List<UserResult> = transaction {
        val users = UsersTable.selectAll()
            .where {
                (UsersTable.isPublic eq true) and
                        (UsersTable.name.lowerCase() like "%${query.lowercase()}%")
            }
            .limit(20)
            .toList()

        users.map { row ->
            val pkgCount = PackagesTable.selectAll()
                .where {
                    (PackagesTable.userId eq row[UsersTable.id]) and
                            (PackagesTable.isPublic eq true) and
                            (PackagesTable.deletedAt.isNull() as Op<Boolean>)
                }
                .count().toInt()

            UserResult(
                id           = row[UsersTable.id],
                name         = row[UsersTable.name],
                photoUrl     = row[UsersTable.photoUrl],
                description  = row[UsersTable.description],
                packageCount = pkgCount
            )
        }
    }

    // busca paquetes por tag exacto
    fun searchByTag(tag: String): List<SearchResult> = transaction {
        val join = PackagesTable
            .join(UsersTable, JoinType.INNER, PackagesTable.userId, UsersTable.id)

        join.selectAll()
            .where {
                PackagesTable.isPublic eq true and
                        (PackagesTable.deletedAt.isNull() as Op<Boolean>) and
                        (PackagesTable.tags.lowerCase() like "%${tag.lowercase()}%")
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .limit(20)
            .map { rowToResult(it) }
    }

    // tags populares — las más usadas
    fun popularTags(): List<TagResult> = transaction {
        val allTags = PackagesTable.selectAll()
            .where { PackagesTable.isPublic eq true and (PackagesTable.deletedAt.isNull() as Op<Boolean>) }
            .map { it[PackagesTable.tags] }
            .flatMap { parseTags(it) }

        allTags.groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(20)
            .map { TagResult(tag = it.key, packageCount = it.value) }
    }

    fun trending(limit: Int = 10): List<SearchResult> = transaction {
        val join = PackagesTable
            .join(UsersTable, JoinType.INNER, PackagesTable.userId, UsersTable.id)

        val allPublic = join.selectAll()
            .where {
                PackagesTable.isPublic eq true and
                        (PackagesTable.deletedAt.isNull() as Op<Boolean>)
            }
            .orderBy(PackagesTable.id, SortOrder.DESC)
            .map { rowToResult(it) }

        val rated = allPublic
            .filter { it.avgRating != null }
            .sortedWith(
                compareByDescending<SearchResult> { it.avgRating }
                    .thenByDescending { it.reviewCount }
            )

        if (rated.size >= limit) {
            rated.take(limit)
        } else {
            val ratedIds = rated.map { it.id }.toSet()
            val fillers = allPublic
                .filter { it.id !in ratedIds }
                .take(limit - rated.size)
            rated + fillers
        }
    }
}

