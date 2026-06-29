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
        require(body.question.isNotBlank()) { "La pregunta no puede estar vacia" }
        require(body.answer.isNotBlank()) { "La respuesta no puede estar vacia" }

        val ownerId = PackageRepository.getOwnerId(packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(ownerId == userId) { "No tienes permiso para modificar este paquete" }

        return CardRepository.create(
            packageId = packageId,
            question  = body.question.trim(),
            answer    = body.answer.trim()
        )
    }

    fun update(id: Int, userId: String, body: UpdateCardRequest): Card? {
        val card = CardRepository.findById(id)
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val ownerId = PackageRepository.getOwnerId(card.packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(ownerId == userId) { "No tienes permiso para modificar esta tarjeta" }

        return CardRepository.update(
            id       = id,
            question = body.question?.trim(),
            answer   = body.answer?.trim()
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        val card = CardRepository.findById(id)
            ?: throw IllegalArgumentException("Tarjeta no encontrada")

        val ownerId = PackageRepository.getOwnerId(card.packageId)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(ownerId == userId) { "No tienes permiso para eliminar esta tarjeta" }

        return CardRepository.delete(id)
    }
}