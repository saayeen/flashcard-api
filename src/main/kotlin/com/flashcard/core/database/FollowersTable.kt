package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object FollowersTable : Table("followers") {
    val followerId  = varchar("follower_id", 128)
    val followingId = varchar("following_id", 128)

    override val primaryKey = PrimaryKey(followerId, followingId)
}

object ReviewsTable : Table("reviews") {
    val id        = integer("id").autoIncrement()
    val userId    = varchar("user_id", 128).references(UsersTable.id)
    val packageId = integer("package_id").references(PackagesTable.id)
    val rating    = integer("rating")
    val comment   = text("comment").default("")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uq_review_user_package", userId, packageId)
    }
}