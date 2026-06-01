package com.flashcard.modules.folders

import com.flashcard.core.database.FolderPackagesTable
import com.flashcard.core.database.FoldersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object FolderRepository {

    private fun rowToFolder(row: ResultRow) = Folder(
        id     = row[FoldersTable.id],
        userId = row[FoldersTable.userId],
        name   = row[FoldersTable.name],
        color  = row[FoldersTable.color]
    )

    fun findAllByUser(userId: String): List<Folder> = transaction {
        FoldersTable.selectAll()
            .where { FoldersTable.userId eq userId and (FoldersTable.deletedAt.isNull()) }
            .map { rowToFolder(it) }
    }

    fun findById(id: Int): Folder? = transaction {
        FoldersTable.selectAll()
            .where { FoldersTable.id eq id and (FoldersTable.deletedAt.isNull()) }
            .map { rowToFolder(it) }
            .singleOrNull()
    }

    fun create(userId: String, name: String, color: String): Folder = transaction {
        val newId = FoldersTable.insert {
            it[FoldersTable.userId] = userId
            it[FoldersTable.name]   = name
            it[FoldersTable.color]  = color
        } get FoldersTable.id
        findById(newId)!!
    }

    fun update(id: Int, name: String?, color: String?): Folder? = transaction {
        FoldersTable.update({ FoldersTable.id eq id }) {
            if (name != null)  it[FoldersTable.name]  = name
            if (color != null) it[FoldersTable.color] = color
        }
        findById(id)
    }

    fun delete(id: Int): Boolean = transaction {
        val updated = FoldersTable.update({ FoldersTable.id eq id }) {
            it[FoldersTable.deletedAt] = LocalDateTime.now()
        }
        updated > 0
    }

    fun addPackage(folderId: Int, packageId: Int): Boolean = transaction {
        try {
            FolderPackagesTable.insert {
                it[FolderPackagesTable.folderId]  = folderId
                it[FolderPackagesTable.packageId] = packageId
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removePackage(folderId: Int, packageId: Int): Boolean = transaction {
        val deleted = FolderPackagesTable.deleteWhere {
            FolderPackagesTable.folderId eq folderId and (FolderPackagesTable.packageId eq packageId)
        }
        deleted > 0
    }

    fun getPackageIds(folderId: Int): List<Int> = transaction {
        FolderPackagesTable.selectAll()
            .where { FolderPackagesTable.folderId eq folderId }
            .map { it[FolderPackagesTable.packageId] }
    }
}