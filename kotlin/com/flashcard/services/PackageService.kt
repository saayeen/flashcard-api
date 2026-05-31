package com.flashcard.services

import com.flashcard.models.CreatePackageRequest
import com.flashcard.models.FlashcardPackage
import com.flashcard.repositories.PackageRepository

// La capa de servicio contiene la lógica de negocio
// equivalente a un service en NestJS o un controller gordo en Express
// aquí van las reglas: validaciones, permisos, transformaciones
object PackageService {

    fun getAll(): List<FlashcardPackage> {
        return PackageRepository.findAll()
    }

    fun getById(id: Int): FlashcardPackage? {
        return PackageRepository.findById(id)
    }

    fun create(body: CreatePackageRequest, userId: String): FlashcardPackage {
        // aquí puedes agregar reglas de negocio
        // por ejemplo: validar que el nombre no esté vacío
        require(body.name.isNotBlank()) { "El nombre no puede estar vacío" }
        require(body.description.isNotBlank()) { "La descripción no puede estar vacía" }

        return PackageRepository.create(
            name        = body.name.trim(),
            description = body.description.trim(),
            category    = body.category.trim(),
            isPublic    = body.isPublic
        )
    }

    fun delete(id: Int, userId: String): Boolean {
        // más adelante aquí verificarías que userId es el dueño del paquete
        val pkg = PackageRepository.findById(id) ?: return false
        return PackageRepository.delete(id)
    }
}