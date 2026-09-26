package com.flashcard.core.database

import org.jetbrains.exposed.sql.Table

object FolderPackagesTable : Table("folder_packages") {
    val folderId  = integer("folder_id")
    val packageId = integer("package_id")

    override val primaryKey = PrimaryKey(folderId, packageId)
}