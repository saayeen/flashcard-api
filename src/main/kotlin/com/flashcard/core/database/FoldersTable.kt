package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object FoldersTable : Table("folders") {
    val id        = integer("id").autoIncrement()
    val userId    = varchar("user_id", 128)
    val name      = varchar("name", 255)
    val color     = varchar("color", 7).default("#6366f1")
    val deletedAt = datetime("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object FolderPackagesTable : Table("folder_packages") {
    val folderId  = integer("folder_id")
    val packageId = integer("package_id")

    override val primaryKey = PrimaryKey(folderId, packageId)
}