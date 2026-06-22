package com.flashcard.core.security

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken

object FirebaseAdmin {

    fun init() {
        val credentials = buildCredentials()

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }

    private fun buildCredentials(): GoogleCredentials {
        // En producción (Railway): viene como variable de entorno con el JSON completo
        val json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        if (json != null) {
            return GoogleCredentials.fromStream(json.byteInputStream())
        }

        // En local: lee el archivo como antes
        val stream = FirebaseAdmin::class.java
            .classLoader
            .getResourceAsStream("firebase-service-account.json")
            ?: throw IllegalStateException("No se encontró firebase-service-account.json")

        return GoogleCredentials.fromStream(stream)
    }

    fun verifyToken(idToken: String): FirebaseToken? {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: Exception) {
            null
        }
    }
}