package com.flashcard.modules.cards

import com.flashcard.modules.packages.PackageRepository
import kotlinx.serialization.internal.throwMissingFieldException

object CardService {

    fun getByPackage(packageId: Int): List<Card> {
        return CardRepository.getEffectiveCards(packageId)
    }

    fun getById(id: Int): Card? {
        return CardRepository.findById(id)
    }

    fun create(packageId: Int, userId: String, body: CreateCardRequest): Card {
        val packageOwnerId = PackageRepository.getOwnerId(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(packageOwnerId == userId) { "No tienes permiso para agregar tarjetas a este paquete" }

        require(body.question.isNotBlank()) { "La pregunta no puede estar vacia" }
        require(body.answer.isNotBlank()) { "La respuesta no puede estar vacia" }

        return CardRepository.create(
            packageId = packageId,
            question  = body.question.trim(),
            answer    = body.answer.trim()
        )
    }

    fun update(packageId: Int, cardId: Int, userId: String, question: String?, answer: String?): Card {
        val card = CardRepository.findById(cardId)
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val contextOwnerId = PackageRepository.getOwnerId(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(contextOwnerId == userId) { "No tienes permiso para editar tarjetas en este paquete" }

        return if (card.packageId == packageId) {
            // caso 1: la tarjeta ya es físicamente propia de este paquete (original, o ya sobreescrita antes)
            CardRepository.protectDependentForks(cardId)
            CardRepository.update(cardId, question?.trim(), answer?.trim())
                ?: throw IllegalStateException("Error inesperado actualizando la tarjeta")
        } else {
            // caso 2: la tarjeta es heredada de un original -> crear/actualizar el override en el fork
            CardRepository.editInheritedCard(packageId, cardId, question?.trim(), answer?.trim())
        }
    }

    fun delete(packageId: Int, cardId: Int, userId: String): Boolean {
        val card = CardRepository.findById(cardId)
        // trae la tarjeta real, con su packageId REAL guardado en la BD
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val contextOwnerId = PackageRepository.getOwnerId(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(contextOwnerId == userId)
        { "No tienes permiso para eliminar tarjetas en este paquete" }

        return if (card.packageId == packageId) {
            // packageId acá es el que vino en la URL (el contexto)
            // caso 1: fila física propia de este paquete
            CardRepository.protectDependentForks(cardId)
            CardRepository.delete(cardId)
        } else {
            // caso 2: heredada del original -> excluirla para este fork, sin tocar la fila real
            CardRepository.excludeInheritedCard(packageId, cardId)
            true
        }
    }
}