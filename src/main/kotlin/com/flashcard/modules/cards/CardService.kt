package com.flashcard.modules.cards

import com.flashcard.modules.packages.PackageRepository

object CardService {

    fun getByPackage(packageId: Int): List<Card> {
        return CardRepository.findByPackageId(packageId)
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

    fun update(cardId: Int, userId: String, question: String?, answer: String?): Card {
        val card = CardRepository.findById(cardId)
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val packageOwnerId = PackageRepository.getOwnerId(card.packageId)
        require(packageOwnerId == userId) { "No tienes permiso para editar esta tarjeta" }

        CardRepository.protectDependentForks(cardId)

        return CardRepository.update(
            id       = cardId,
            question = question?.trim(),
            answer   = answer?.trim()
        ) ?: throw IllegalStateException("Error inesperado actualizando la tarjeta") // 👈 nuevo
    }

    fun delete(id: Int, userId: String): Boolean {
        val card = CardRepository.findById(id)
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val packageOwnerId = PackageRepository.getOwnerId(card.packageId)
        require(packageOwnerId == userId) { "No tienes permiso para eliminar esta tarjeta" }

        CardRepository.protectDependentForks(id)   // congela copias en forks antes de borrar el original

        return CardRepository.delete(id)
    }
}