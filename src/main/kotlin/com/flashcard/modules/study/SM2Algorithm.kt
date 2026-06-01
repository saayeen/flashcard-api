package com.flashcard.modules.study

// Implementación del algoritmo SM-2
// Referencia: https://www.supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method
object SM2Algorithm {

    data class SM2Result(
        val easeFactor: Double,
        val intervalDays: Int,
        val nextReviewDays: Int  // días desde hoy hasta próxima revisión
    )

    fun calculate(
        quality: Int,       // 1-4 (respuesta del usuario)
        easeFactor: Double, // factor de facilidad actual (empieza en 2.5)
        intervalDays: Int,  // intervalo actual en días
        repetitions: Int    // cuántas veces ha sido revisada
    ): SM2Result {

        // calidad debe estar entre 1 y 4
        val q = quality.coerceIn(1, 4)

        // convertir escala 1-4 a escala 0-5 que usa SM-2
        // 1=Dificil→1, 2=Casi→2, 3=Bien→3, 4=Facil→5
        val smQuality = when (q) {
            1 -> 1  // Dificil
            2 -> 2  // Casi
            3 -> 3  // Bien
            4 -> 5  // Facil
            else -> 3
        }

        // si la calidad es menor a 3 (Dificil o Casi), reinicia el intervalo
        if (smQuality < 3) {
            return SM2Result(
                easeFactor   = maxOf(1.3, easeFactor - 0.2),
                intervalDays = 1,
                nextReviewDays = 1
            )
        }

        // calcular nuevo factor de facilidad
        // formula original SM-2:
        // EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        val newEF = easeFactor + (0.1 - (5 - smQuality) * (0.08 + (5 - smQuality) * 0.02))
        val clampedEF = maxOf(1.3, newEF)  // EF nunca baja de 1.3

        // calcular nuevo intervalo
        val newInterval = when (repetitions) {
            0    -> 1
            1    -> 6
            else -> (intervalDays * clampedEF).toInt()
        }

        return SM2Result(
            easeFactor     = clampedEF,
            intervalDays   = newInterval,
            nextReviewDays = newInterval
        )
    }
}