package com.flashcard.modules.packages

import com.flashcard.core.database.FolderPackagesTable
import com.flashcard.core.database.PackagesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object FolderPackageRepository {

    fun addPackage(folderId: Int, packageId: Int): Boolean = transaction {
        try {
            FolderPackagesTable.insert {
                it[FolderPackagesTable.folderId] = folderId
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

    fun getPackages(folderId: Int): List<FlashcardPackage> = transaction {
        FolderPackagesTable
            .join(PackagesTable, JoinType.INNER, additionalConstraint = { FolderPackagesTable.packageId eq PackagesTable.id })
            .selectAll()
            .where { FolderPackagesTable.folderId eq folderId and PackagesTable.deletedAt.isNull() }
            .map { row ->
                FlashcardPackage(
                    id          = row[PackagesTable.id],
                    userId      = row[PackagesTable.userId],
                    type        = row[PackagesTable.type],
                    name        = row[PackagesTable.name],
                    description = row[PackagesTable.description],
                    category    = row[PackagesTable.category],
                    cardCount   = row[PackagesTable.cardCount],
                    isPublic    = row[PackagesTable.isPublic],
                    color       = row[PackagesTable.color]
                )
            }
    }
}