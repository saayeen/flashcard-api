package com.flashcard.modules.cards

object CardService {

    fun getByPackage(packageId: Int): List<Card> {
        return CardRepository.findByPackageId(packageId)
    }

    fun getById(id: Int): Card? {
        return CardRepository.findById(id)
    }

    fun create(packageId: Int, body: CreateCardRequest): Card {
        require(body.question.isNotBlank()) { "La pregunta no puede estar vacia" }
        require(body.answer.isNotBlank()) { "La respuesta no puede estar vacia" }
        return CardRepository.create(
            packageId = packageId,
            question  = body.question.trim(),
            answer    = body.answer.trim()
        )
    }

    fun update(id: Int, body: UpdateCardRequest): Card? {
        return CardRepository.update(
            id       = id,
            question = body.question?.trim(),
            answer   = body.answer?.trim()
        )
    }

    fun delete(id: Int): Boolean {
        return CardRepository.delete(id)
    }
}