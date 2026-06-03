package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object FollowersTable : Table("followers") {
    val followerId  = varchar("follower_id", 128)
    val followingId = varchar("following_id", 128)

    override val primaryKey = PrimaryKey(followerId, followingId)
}

object ReviewsTable : Table("reviews") {
    val id        = integer("id").autoIncrement()
    val userId    = varchar("user_id", 128)
    val packageId = integer("package_id")
    val rating    = integer("rating")
    val comment   = text("comment").default("")

    override val primaryKey = PrimaryKey(id)
}