package com.flashcard.modules.packages

object PackageService {

    fun getAll(): List<FlashcardPackage> = PackageRepository.findAllPublic()

    fun getById(id: Int): FlashcardPackage? = PackageRepository.findById(id)

    fun create(body: CreatePackageRequest, userId: String): FlashcardPackage {
        return PackageRepository.create(
            userId      = userId,
            name        = body.name.trim(),
            description = body.description.trim(),
            category    = body.category.trim(),
            isPublic    = body.isPublic,
            theme       = body.theme
        )
    }

    fun update(id: Int, userId: String, body: UpdatePackageRequest): FlashcardPackage? {
        val owner = PackageRepository.getOwnerId(id)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(owner == userId) { "No tienes permiso para editar este paquete" }
        return PackageRepository.update(
            id          = id,
            name        = body.name?.trim(),
            description = body.description?.trim(),
            category    = body.category?.trim(),
            isPublic    = body.isPublic
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        val owner = PackageRepository.getOwnerId(id) ?: return false
        require(owner == userId) { "No tienes permiso para eliminar este paquete" }
        return PackageRepository.delete(id)
    }
}