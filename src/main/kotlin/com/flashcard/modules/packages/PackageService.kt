package com.flashcard.modules.packages

import com.flashcard.modules.cards.CardRepository

object PackageService {

    fun getAll(): List<FlashcardPackage> {
        return PackageRepository.findAll()
    }

    fun getById(id: Int): FlashcardPackage? {
        return PackageRepository.findById(id)
    }

    fun create(body: CreatePackageRequest, userId: String): FlashcardPackage {
        return PackageRepository.create(
            userId      = userId,
            name        = body.name.trim(),
            description = body.description.trim(),
            category    = body.category.trim(),
            isPublic    = body.isPublic
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        val owner = PackageRepository.getOwnerId(id) ?: return false
        require(owner == userId) { "No tienes permiso para eliminar este paquete" }

        CardRepository.protectDependentForksOnPackageDelete(id)  // 👈 nuevo, antes del borrado

        return PackageRepository.delete(id)
    }
}