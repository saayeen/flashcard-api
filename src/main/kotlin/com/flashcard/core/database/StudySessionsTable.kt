package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object StudySessionsTable : Table("study_sessions") {
    val id         = integer("id").autoIncrement()
    val userId     = varchar("user_id", 128)
    val packageId  = integer("package_id")
    val startedAt  = datetime("started_at")
    val finishedAt = datetime("finished_at").nullable()

    override val primaryKey = PrimaryKey(id)
}