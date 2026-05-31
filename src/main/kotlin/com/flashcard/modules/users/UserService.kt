package com.flashcard.modules.users

import com.flashcard.core.security.FirebaseAdmin

object UserService {

    fun loginOrRegister(idToken: String): User? {
        val decoded = FirebaseAdmin.verifyToken(idToken) ?: return null
        return UserRepository.findOrCreate(
            id       = decoded.uid,
            email    = decoded.email ?: "",
            name     = decoded.name ?: "Usuario",
            photoUrl = decoded.picture
        )
    }

    fun getProfile(userId: String): User? {
        return UserRepository.findById(userId)
    }

    fun updateProfile(userId: String, body: UpdateUserRequest): User? {
        return UserRepository.update(
            id          = userId,
            name        = body.name,
            description = body.description,
            isPublic    = body.isPublic
        )
    }
}