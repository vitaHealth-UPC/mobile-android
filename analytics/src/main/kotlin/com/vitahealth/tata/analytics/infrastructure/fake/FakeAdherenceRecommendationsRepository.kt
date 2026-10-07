package com.vitahealth.tata.analytics.infrastructure.fake

import com.vitahealth.tata.analytics.application.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecommendationReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class FakeRecommendationsScenario {
    Content,
    InsufficientEvidence,
    Error,
}

/**
 * Datos de ejemplo mientras el backend de analytics no está disponible.
 * Se reemplaza por una implementación remota que cumpla AdherenceRecommendationsRepository.
 */
class FakeAdherenceRecommendationsRepository(
    private val scenario: FakeRecommendationsScenario = FakeRecommendationsScenario.Content,
) : AdherenceRecommendationsRepository {
    override suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?> {
        delay(600.milliseconds)
        return when (scenario) {
            FakeRecommendationsScenario.Content -> AppResult.Success(sample(periodDays))
            FakeRecommendationsScenario.InsufficientEvidence -> AppResult.Success(null)
            FakeRecommendationsScenario.Error -> AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private fun sample(periodDays: Int) = AdherenceRecommendationsReadModel(
        periodDays = periodDays,
        patternTitle = "Patrón de omisiones por la tarde",
        patternSummary = "4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.",
        concentration = listOf(
            listOf(0.27f, 0.495f, 0.72f, 0.42f, 0.345f, 0.795f, 0.645f),
            listOf(0.27f, 0.42f, 0.645f, 0.57f, 0.345f, 0.72f, 0.795f),
            listOf(0.195f, 0.345f, 0.57f, 0.495f, 0.27f, 0.645f, 0.72f),
        ),
        recommendations = listOf(
            RecommendationReadModel("Ajusta el recordatorio", "Prueba avisar 15 minutos antes de la toma."),
            RecommendationReadModel("Revisa el horario", "Elige una hora asociada a una rutina estable."),
            RecommendationReadModel("Acompaña durante una semana", "Comprueba si el nuevo horario reduce retrasos."),
        ),
    )
}
