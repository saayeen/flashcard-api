package com.flashcard.modules.collaboration

import com.flashcard.core.database.*
import com.flashcard.modules.packages.FlashcardPackage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CollaborationRepository {

    // alias para traer el nombre del autor original (igual que en PackageRepository)
    private val OriginalAuthorAlias = UsersTable.alias("original_author")

    // ── Fork ─────────────────────────────────────────────────────
    //  revisa si este usuario ya tiene una copia forkeada de este paquete
    fun findExistingFork(originalId: Int, userId: String): FlashcardPackage? = transaction {
        PackagesTable
            .join(UsersTable, JoinType.LEFT, additionalConstraint = { PackagesTable.userId eq UsersTable.id })
            .join(OriginalAuthorAlias, JoinType.LEFT, additionalConstraint = { PackagesTable.originalAuthorId eq OriginalAuthorAlias[UsersTable.id] })
            .selectAll()
            .where {
                PackagesTable.forkedFromId eq originalId and
                        (PackagesTable.userId eq userId) and
                        (PackagesTable.deletedAt.isNull() as Op<Boolean>)
            }
            .map { row ->
                FlashcardPackage(
                    id                 = row[PackagesTable.id],
                    userId             = row[PackagesTable.userId],
                    name               = row[PackagesTable.name],
                    description        = row[PackagesTable.description],
                    category           = row[PackagesTable.category],
                    cardCount          = row[PackagesTable.cardCount],
                    isPublic           = row[PackagesTable.isPublic],
                    theme              = row[PackagesTable.theme] ?: "default",
                    userName           = row[UsersTable.name],
                    userPhotoUrl       = row[UsersTable.photoUrl],
                    forkedFromId       = row[PackagesTable.forkedFromId],
                    originalAuthorId   = row[PackagesTable.originalAuthorId],
                    originalAuthorName = row.getOrNull(OriginalAuthorAlias[UsersTable.name])
                )
            }
            .singleOrNull()
    }

    fun forkPackage(originalId: Int, userId: String): FlashcardPackage = transaction {
        val original = PackagesTable.selectAll()
            .where { PackagesTable.id eq originalId }
            .single()

        val newId = PackagesTable.insert {
            it[PackagesTable.userId]          = userId
            it[PackagesTable.name]            = original[PackagesTable.name]
            it[PackagesTable.description]     = original[PackagesTable.description]
            it[PackagesTable.category]        = original[PackagesTable.category]
            it[PackagesTable.isPublic]        = false
            it[PackagesTable.forkedFromId]    = originalId
            it[PackagesTable.originalAuthorId] = original[PackagesTable.originalAuthorId] ?: original[PackagesTable.userId]
        } get PackagesTable.id

        val originalCards = CardsTable.selectAll()
            .where { CardsTable.packageId eq originalId and (CardsTable.deletedAt.isNull()) }

        for (card in originalCards) {
            CardsTable.insert {
                it[CardsTable.packageId] = newId
                it[CardsTable.question]  = card[CardsTable.question]
                it[CardsTable.answer]    = card[CardsTable.answer]
            }
        }

        // arreglo del cardCount
        val totalCards = originalCards.count().toInt()
        PackagesTable.update({ PackagesTable.id eq newId }) {
            it[PackagesTable.cardCount] = totalCards
        }

        PackagesTable
            .join(UsersTable, JoinType.LEFT, additionalConstraint = { PackagesTable.userId eq UsersTable.id })
            .join(OriginalAuthorAlias, JoinType.LEFT, additionalConstraint = { PackagesTable.originalAuthorId eq OriginalAuthorAlias[UsersTable.id] })
            .selectAll()
            .where { PackagesTable.id eq newId }
            .map { row ->
                FlashcardPackage(
                    id                 = row[PackagesTable.id],
                    userId             = row[PackagesTable.userId],
                    name               = row[PackagesTable.name],
                    description        = row[PackagesTable.description],
                    category           = row[PackagesTable.category],
                    cardCount          = row[PackagesTable.cardCount],
                    isPublic           = row[PackagesTable.isPublic],
                    theme              = row[PackagesTable.theme] ?: "default",
                    userName           = row[UsersTable.name],
                    userPhotoUrl       = row[UsersTable.photoUrl],
                    forkedFromId       = row[PackagesTable.forkedFromId],
                    originalAuthorId   = row[PackagesTable.originalAuthorId],
                    originalAuthorName = row.getOrNull(OriginalAuthorAlias[UsersTable.name])
                )
            }.first()
    }


    // ── Follow (toggle) ───────────────────────────────────────────
    // devuelve true si ahora sigue, false si dejó de seguir
    fun toggleFollow(followerId: String, followingId: String): Boolean = transaction {
        val exists = FollowersTable.selectAll()
            .where {
                FollowersTable.followerId eq followerId and
                        (FollowersTable.followingId eq followingId)
            }.count() > 0

        if (exists) {
            FollowersTable.deleteWhere {
                FollowersTable.followerId eq followerId and
                        (FollowersTable.followingId eq followingId)
            }
            false
        } else {
            FollowersTable.insert {
                it[FollowersTable.followerId]  = followerId
                it[FollowersTable.followingId] = followingId
            }
            true
        }
    }

    fun isFollowing(followerId: String, followingId: String): Boolean = transaction {
        FollowersTable.selectAll()
            .where {
                FollowersTable.followerId eq followerId and
                        (FollowersTable.followingId eq followingId)
            }.count() > 0
    }

    fun getFollowersCount(userId: String): Int = transaction {
        FollowersTable.selectAll()
            .where { FollowersTable.followingId eq userId }
            .count().toInt()
    }

    fun getFollowingCount(userId: String): Int = transaction {
        FollowersTable.selectAll()
            .where { FollowersTable.followerId eq userId }
            .count().toInt()
    }

    fun getFollowers(userId: String): List<String> = transaction {
        FollowersTable.selectAll()
            .where { FollowersTable.followingId eq userId }
            .map { it[FollowersTable.followerId] }
    }

    fun getFollowing(userId: String): List<String> = transaction {
        FollowersTable.selectAll()
            .where { FollowersTable.followerId eq userId }
            .map { it[FollowersTable.followingId] }
    }

    // ── Reviews ───────────────────────────────────────────────────
    fun createReview(userId: String, packageId: Int, rating: Int, comment: String): Review = transaction {
        val exists = ReviewsTable.selectAll()
            .where { ReviewsTable.userId eq userId and (ReviewsTable.packageId eq packageId) }
            .count() > 0
        require(!exists) { "Ya has reseñado este paquete" }

        val newId = ReviewsTable.insert {
            it[ReviewsTable.userId]    = userId
            it[ReviewsTable.packageId] = packageId
            it[ReviewsTable.rating]    = rating
            it[ReviewsTable.comment]   = comment
        } get ReviewsTable.id

        (ReviewsTable innerJoin UsersTable)
            .selectAll()
            .where { ReviewsTable.id eq newId }
            .map { row ->
                Review(
                    id           = row[ReviewsTable.id],
                    userId       = row[ReviewsTable.userId],
                    packageId    = row[ReviewsTable.packageId],
                    rating       = row[ReviewsTable.rating],
                    comment      = row[ReviewsTable.comment],
                    userName     = row[UsersTable.name],
                    userPhotoUrl = row[UsersTable.photoUrl]
                )
            }.first()
    }

    fun getReviews(packageId: Int): List<Review> = transaction {
        (ReviewsTable innerJoin UsersTable)
            .selectAll()
            .where { ReviewsTable.packageId eq packageId }
            .map { row ->
                Review(
                    id           = row[ReviewsTable.id],
                    userId       = row[ReviewsTable.userId],
                    packageId    = row[ReviewsTable.packageId],
                    rating       = row[ReviewsTable.rating],
                    comment      = row[ReviewsTable.comment],
                    userName     = row[UsersTable.name],
                    userPhotoUrl = row[UsersTable.photoUrl]
                )
            }
    }

    fun deleteReview(userId: String, packageId: Int): Boolean = transaction {
        ReviewsTable.deleteWhere {
            ReviewsTable.userId eq userId and (ReviewsTable.packageId eq packageId)
        } > 0
    }

    fun follow(followerId: String, followingId: String): Boolean = transaction {
        try {
            FollowersTable.insert {
                it[FollowersTable.followerId]  = followerId
                it[FollowersTable.followingId] = followingId
            }
            true
        } catch (e: Exception) { false }
    }

    fun unfollow(followerId: String, followingId: String): Boolean = transaction {
        FollowersTable.deleteWhere {
            FollowersTable.followerId eq followerId and
                    (FollowersTable.followingId eq followingId)
        } > 0
    }
}