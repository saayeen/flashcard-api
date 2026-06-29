package com.flashcard.modules.cards

import com.flashcard.core.database.CardsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

package com.flashcard.modules.cards

import com.flashcard.core.database.CardsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object CardRepository {

    private fun rowToCard(row: ResultRow) = Card(
        id        = row[CardsTable.id],
        packageId = row[CardsTable.packageId],
        question  = row[CardsTable.question],
        answer    = row[CardsTable.answer]
    )

    fun findByPackageId(packageId: Int): List<Card> = transaction {
        CardsTable.selectAll()
            .where { CardsTable.packageId eq packageId and (CardsTable.deletedAt.isNull()) }
            .map { rowToCard(it) }
    }

    fun findById(id: Int): Card? = transaction {
        CardsTable.selectAll()
            .where { CardsTable.id eq id and (CardsTable.deletedAt.isNull()) }
            .map { rowToCard(it) }
            .singleOrNull()
    }

    fun create(packageId: Int, question: String, answer: String): Card = transaction {
        val newId = CardsTable.insert {
            it[CardsTable.packageId] = packageId
            it[CardsTable.question]  = question
            it[CardsTable.answer]    = answer
        } get CardsTable.id
        findById(newId)!!
    }

    fun update(id: Int, question: String?, answer: String?): Card? = transaction {
        CardsTable.update({ CardsTable.id eq id }) {
            if (question != null) it[CardsTable.question] = question
            if (answer != null)   it[CardsTable.answer]   = answer
        }
        findById(id)
    }

    // soft delete — no borra el registro, solo marca deletedAt
    fun delete(id: Int): Boolean = transaction {
        val updated = CardsTable.update({ CardsTable.id eq id }) {
            it[CardsTable.deletedAt] = LocalDateTime.now()
        }
        updated > 0
    }
}