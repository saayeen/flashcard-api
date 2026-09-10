package com.flashcard.modules.cards

import com.flashcard.core.database.CardsTable
import com.flashcard.core.database.PackagesTable
import com.flashcard.core.database.CardExclusionsTable
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

    // ── Copy-on-write ────────────────────────────────────────────

    // Devuelve las tarjetas que un paquete "ve" realmente, sea original o fork
    fun getEffectiveCards(packageId: Int): List<Card> = transaction {
        val pkg = PackagesTable.selectAll()
            .where { PackagesTable.id eq packageId }
            .single()

        val ownCardsRows = CardsTable.selectAll()
            .where { CardsTable.packageId eq packageId and CardsTable.deletedAt.isNull() }
            .toList()

        val forkedFromId = pkg[PackagesTable.forkedFromId]
            ?: return@transaction ownCardsRows.map { rowToCard(it) } // paquete original: no hay nada más que resolver

        val excludedIds = CardExclusionsTable.selectAll()
            .where { CardExclusionsTable.packageId eq packageId }
            .map { it[CardExclusionsTable.originalCardId] }
            .toSet()

        val overriddenSourceIds = ownCardsRows
            .mapNotNull { it[CardsTable.sourceCardId] }
            .toSet()

        val inheritedFromOriginal = CardsTable.selectAll()
            .where {
                CardsTable.packageId eq forkedFromId and
                        CardsTable.deletedAt.isNull() and
                        (CardsTable.id notInList (excludedIds + overriddenSourceIds).ifEmpty { listOf(-1) })
            }
            .toList()

        (ownCardsRows + inheritedFromOriginal).map { rowToCard(it) }
    }

    // Si el fork todavía depende en vivo de esta tarjeta, la copia hacia su propio packageId.
    private fun materializeIfNeeded(forkPackageId: Int, originalCard: ResultRow) {
        val originalCardId = originalCard[CardsTable.id]

        val alreadyExcluded = CardExclusionsTable.selectAll()
            .where {
                CardExclusionsTable.packageId eq forkPackageId and
                        (CardExclusionsTable.originalCardId eq originalCardId)
            }.count() > 0
        if (alreadyExcluded) return

        val alreadyOverridden = CardsTable.selectAll()
            .where {
                CardsTable.packageId eq forkPackageId and
                        (CardsTable.sourceCardId eq originalCardId)
            }.count() > 0
        if (alreadyOverridden) return

        CardsTable.insert {
            it[CardsTable.packageId]    = forkPackageId
            it[CardsTable.question]     = originalCard[CardsTable.question]
            it[CardsTable.answer]       = originalCard[CardsTable.answer]
            it[CardsTable.sourceCardId] = originalCardId
        }
    }

    // Llamar ANTES de aplicar un update/delete sobre una tarjeta del original
    fun protectDependentForks(originalCardId: Int) = transaction {
        val originalCard = CardsTable.selectAll()
            .where { CardsTable.id eq originalCardId }
            .single()

        val originalPackageId = originalCard[CardsTable.packageId]

        val activeForks = PackagesTable.selectAll()
            .where {
                PackagesTable.forkedFromId eq originalPackageId and
                        PackagesTable.deletedAt.isNull()
            }
            .map { it[PackagesTable.id] }

        activeForks.forEach { forkId ->
            materializeIfNeeded(forkId, originalCard)
        }
    }

    // Igual, pero para cuando se borra el PAQUETE completo
    fun protectDependentForksOnPackageDelete(originalPackageId: Int) = transaction {
        val activeForks = PackagesTable.selectAll()
            .where {
                PackagesTable.forkedFromId eq originalPackageId and
                        PackagesTable.deletedAt.isNull()
            }
            .map { it[PackagesTable.id] }

        val originalCards = CardsTable.selectAll()
            .where { CardsTable.packageId eq originalPackageId and CardsTable.deletedAt.isNull() }
            .toList()

        activeForks.forEach { forkId ->
            originalCards.forEach { card -> materializeIfNeeded(forkId, card) }
        }
    }
}