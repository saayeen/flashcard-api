package com.flashcard.modules.collaboration

import com.flashcard.core.database.*
import com.flashcard.modules.packages.FlashcardPackage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CollaborationRepository {

    // ── Fork ─────────────────────────────────────────────────────
    fun forkPackage(originalId: Int, userId: String): FlashcardPackage = transaction {
        val original = PackagesTable.selectAll()
            .where { PackagesTable.id eq originalId }
            .single()

        val newId = PackagesTable.insert {
            it[PackagesTable.userId] = userId
            it[PackagesTable.name] = original[PackagesTable.name]
            it[PackagesTable.description] = original[PackagesTable.description]
            it[PackagesTable.category] = original[PackagesTable.category]
            it[PackagesTable.isPublic] = false
            it[PackagesTable.forkedFromId] = originalId
            it[PackagesTable.originalAuthorId] = original[PackagesTable.userId]
        } get PackagesTable.id

        // copiar todas las tarjetas del paquete original
        val originalCards = CardsTable.selectAll()
            .where { CardsTable.packageId eq originalId and (CardsTable.deletedAt.isNull()) }

        for (card in originalCards) {
            CardsTable.insert {
                it[CardsTable.packageId] = newId
                it[CardsTable.question] = card[CardsTable.question]
                it[CardsTable.answer] = card[CardsTable.answer]
            }
        }

        PackagesTable.selectAll()
            .where { PackagesTable.id eq newId }
            .map { row ->
                FlashcardPackage(
                    id = row[PackagesTable.id],
                    name = row[PackagesTable.name],
                    description = row[PackagesTable.description],
                    category = row[PackagesTable.category],
                    cardCount = row[PackagesTable.cardCount],
                    isPublic = row[PackagesTable.isPublic]
                )
            }.first()
    }

    // ── Follow ────────────────────────────────────────────────────
    fun follow(followerId: String, followingId: String): Boolean = transaction {
        try {
            FollowersTable.insert {
                it[FollowersTable.followerId] = followerId
                it[FollowersTable.followingId] = followingId
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun unfollow(followerId: String, followingId: String): Boolean = transaction {
        val deleted = FollowersTable.deleteWhere {
            FollowersTable.followerId eq followerId and (FollowersTable.followingId eq followingId)
        }
        deleted > 0
    }

    fun isFollowing(followerId: String, followingId: String): Boolean = transaction {
        FollowersTable.selectAll()
            .where { FollowersTable.followerId eq followerId and (FollowersTable.followingId eq followingId) }
            .count() > 0
    }

    // ── Reviews ───────────────────────────────────────────────────
    fun createReview(userId: String, packageId: Int, rating: Int, comment: String): Review = transaction {
        val newId = ReviewsTable.insert {
            it[ReviewsTable.userId] = userId
            it[ReviewsTable.packageId] = packageId
            it[ReviewsTable.rating] = rating
            it[ReviewsTable.comment] = comment
        } get ReviewsTable.id

        ReviewsTable.selectAll()
            .where { ReviewsTable.id eq newId }
            .map { row ->
                Review(
                    id = row[ReviewsTable.id],
                    userId = row[ReviewsTable.userId],
                    packageId = row[ReviewsTable.packageId],
                    rating = row[ReviewsTable.rating],
                    comment = row[ReviewsTable.comment]
                )
            }.first()
    }

    fun getReviews(packageId: Int): List<Review> = transaction {
        ReviewsTable.selectAll()
            .where { ReviewsTable.packageId eq packageId }
            .map { row ->
                Review(
                    id = row[ReviewsTable.id],
                    userId = row[ReviewsTable.userId],
                    packageId = row[ReviewsTable.packageId],
                    rating = row[ReviewsTable.rating],
                    comment = row[ReviewsTable.comment]
                )
            }
    }

}