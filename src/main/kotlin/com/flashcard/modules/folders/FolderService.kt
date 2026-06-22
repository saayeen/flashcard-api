package com.flashcard.modules.folders
import com.flashcard.modules.packages.FlashcardPackage
import com.flashcard.modules.packages.PackageRepository
object FolderService {

    fun getAll(userId: String): List<Folder> {
        return FolderRepository.findAllByUser(userId)
    }

    fun getById(id: Int): Folder? {
        return FolderRepository.findById(id)
    }

    fun create(userId: String, body: CreateFolderRequest): Folder {
        require(body.name.isNotBlank()) { "El nombre no puede estar vacio" }
        return FolderRepository.create(userId, body.name.trim(), body.color)
    }

    fun update(id: Int, body: UpdateFolderRequest): Folder? {
        return FolderRepository.update(id, body.name?.trim(), body.color)
    }

    fun delete(id: Int): Boolean {
        return FolderRepository.delete(id)
    }

    fun addPackage(folderId: Int, packageId: Int): Boolean {
        return FolderRepository.addPackage(folderId, packageId)
    }

    fun removePackage(folderId: Int, packageId: Int): Boolean {
        return FolderRepository.removePackage(folderId, packageId)
    }

    fun getPackagesInFolder(folderId: Int): List<FlashcardPackage> {
        val ids = FolderRepository.getPackageIds(folderId)
        return ids.mapNotNull { PackageRepository.findById(it) }
    }

    fun getPackageIds(folderId: Int): List<Int> {
        return FolderRepository.getPackageIds(folderId)
    }
}