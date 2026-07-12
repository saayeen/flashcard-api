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
            theme       = body.theme,
            tags        = body.tags.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        )
    }

    fun update(id: Int, userId: String, body: UpdatePackageRequest): FlashcardPackage? {
        val existing = PackageRepository.findById(id)
            ?: throw IllegalArgumentException("Paquete no encontrado")
        require(existing.userId == userId) { "No tienes permiso para editar este paquete" }

        // los paquetes forkeados no pueden cambiar nombre ni categoría —
        // deben mantener coherencia con el paquete original
        val isFork = existing.forkedFromId != null

        return PackageRepository.update(
            id          = id,
            name        = if (isFork) null else body.name?.trim(),
            description = body.description?.trim(),
            category    = if (isFork) null else body.category?.trim(),
            isPublic    = body.isPublic,
            tags        = body.tags?.map { it.trim().lowercase() }?.filter { it.isNotBlank() }
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        val owner = PackageRepository.getOwnerId(id) ?: return false
        require(owner == userId) { "No tienes permiso para eliminar este paquete" }
        return PackageRepository.delete(id)
    }
}