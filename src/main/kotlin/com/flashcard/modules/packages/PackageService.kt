package com.flashcard.modules.packages

object PackageService {

    fun getAll(): List<FlashcardPackage> {
        return PackageRepository.findAll()
    }

    fun getById(id: Int): FlashcardPackage? {
        return PackageRepository.findById(id)
    }

    fun create(body: CreatePackageRequest, userId: String): FlashcardPackage {
        return PackageRepository.create(
            userId = userId,
            name = body.name.trim(),
            description = body.description.trim(),
            category = body.category.trim(),
            isPublic = body.isPublic,
            theme = body.theme
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        PackageRepository.findById(id) ?: return false
        return PackageRepository.delete(id)
    }


}