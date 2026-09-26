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

        CardRepository.protectDependentForksOnPackageDelete(id)

        return PackageRepository.delete(id)
    }

    // ── Folders ──────────────────────────────────────────────

    fun getAllFolders(userId: String): List<FlashcardPackage> {
        return PackageRepository.findAllFoldersByUser(userId)
    }

    fun createFolder(userId: String, name: String, color: String): FlashcardPackage {
        require(name.isNotBlank()) { "El nombre no puede estar vacio" }
        return PackageRepository.create(
            userId   = userId,
            name     = name.trim(),
            type     = "folder",
            color    = color,
            isPublic = false
        )
    }

    fun updateFolder(id: Int, userId: String, name: String?, color: String?): FlashcardPackage {
        val existing = PackageRepository.findById(id)
            ?: throw IllegalArgumentException("Carpeta no encontrada")
        require(existing.type == "folder") { "El paquete indicado no es una carpeta" }
        require(existing.userId == userId) { "No tienes permiso para editar esta carpeta" }

        return PackageRepository.update(id, name?.trim(), color)
            ?: throw IllegalStateException("Error inesperado actualizando la carpeta")
    }

    fun deleteFolder(id: Int, userId: String): Boolean {
        val existing = PackageRepository.findById(id)
            ?: throw IllegalArgumentException("Carpeta no encontrada")
        require(existing.type == "folder") { "El paquete indicado no es una carpeta" }
        require(existing.userId == userId) { "No tienes permiso para eliminar esta carpeta" }

        return PackageRepository.delete(id)
    }

    fun addPackageToFolder(folderId: Int, packageId: Int, userId: String) {
        val folder = PackageRepository.findById(folderId)
            ?: throw IllegalArgumentException("Carpeta no encontrada")
        require(folder.type == "folder") { "El destino indicado no es una carpeta" }
        require(folder.userId == userId) { "No tienes permiso sobre esta carpeta" }

        val item = PackageRepository.findById(packageId)
            ?: throw IllegalArgumentException("Elemento no encontrado")
        require(item.type != "folder") { "No se pueden anidar carpetas dentro de carpetas" }

        FolderPackageRepository.addPackage(folderId, packageId)
    }

    fun removePackageFromFolder(folderId: Int, packageId: Int, userId: String) {
        val folder = PackageRepository.findById(folderId)
            ?: throw IllegalArgumentException("Carpeta no encontrada")
        require(folder.userId == userId) { "No tienes permiso sobre esta carpeta" }

        FolderPackageRepository.removePackage(folderId, packageId)
    }

    fun getPackagesInFolder(folderId: Int, userId: String): List<FlashcardPackage> {
        val folder = PackageRepository.findById(folderId)
            ?: throw IllegalArgumentException("Carpeta no encontrada")
        require(folder.userId == userId) { "No tienes permiso sobre esta carpeta" }

        return FolderPackageRepository.getPackages(folderId)
    }
}