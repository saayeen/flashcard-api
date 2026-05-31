package com.flashcard.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken

object FirebaseAdmin {

    fun init() {
        val serviceAccount = FirebaseAdmin::class.java
            .classLoader
            .getResourceAsStream("firebase-service-account.json")
            ?: throw IllegalStateException("No se encontró firebase-service-account.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }

    // Verifica el token JWT que viene del frontend
    // Devuelve el token decodificado si es válido, null si no
    fun verifyToken(idToken: String): FirebaseToken? {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: Exception) {
            null
        }
    }
}