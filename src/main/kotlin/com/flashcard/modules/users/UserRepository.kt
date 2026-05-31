package com.flashcard.modules.users

import com.flashcard.core.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    private fun rowToUser(row: ResultRow) = User(
        id          = row[UsersTable.id],
        email       = row[UsersTable.email],
        name        = row[UsersTable.name],
        photoUrl    = row[UsersTable.photoUrl],
        description = row[UsersTable.description],
        isPublic    = row[UsersTable.isPublic]
    )

    fun findById(id: String): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    fun create(id: String, email: String, name: String, photoUrl: String?): User = transaction {
        UsersTable.insert {
            it[UsersTable.id]       = id
            it[UsersTable.email]    = email
            it[UsersTable.name]     = name
            it[UsersTable.photoUrl] = photoUrl
        }
        findById(id)!!
    }

    fun update(id: String, name: String?, description: String?, isPublic: Boolean?): User? = transaction {
        UsersTable.update({ UsersTable.id eq id }) {
            if (name != null)        it[UsersTable.name]        = name
            if (description != null) it[UsersTable.description] = description
            if (isPublic != null)    it[UsersTable.isPublic]    = isPublic
        }
        findById(id)
    }

    fun findOrCreate(id: String, email: String, name: String, photoUrl: String?): User = transaction {
        findById(id) ?: create(id, email, name, photoUrl)
    }
}