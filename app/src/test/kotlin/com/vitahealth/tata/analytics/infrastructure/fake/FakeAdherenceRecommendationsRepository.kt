package com.vitahealth.tata.analytics.infrastructure.fake

import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceInsightPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

enum class FakeRecommendationsScenario {
    Content,
    InsufficientEvidence,
    Error,
}

/**
 * Datos de ejemplo mientras el backend de analytics no está disponible. Se reemplaza por una
 * implementación remota que cumpla AdherenceRecommendationsRepository.
 */
class FakeAdherenceRecommendationsRepository(
    private val scenario: FakeRecommendationsScenario = FakeRecommendationsScenario.Content
) : AdherenceRecommendationsRepository {
    override suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?> {
        delay(600.milliseconds)
        return when (scenario) {
            FakeRecommendationsScenario.Content -> AppResult.Success(sample(periodDays))
            FakeRecommendationsScenario.InsufficientEvidence -> AppResult.Success(null)
            FakeRecommendationsScenario.Error ->
                AppResult.Failure(
                    message = "No hay conexión. Inténtalo nuevamente.",
                    code = "NETWORK_UNAVAILABLE",
                )
        }
    }

    private fun sample(periodDays: Int) =
        AdherenceRecommendationsReadModel(
            periodDays = periodDays,
            pattern =
                AdherenceInsightPatternReadModel(
                    AdherencePatternType.OMISSION,
                    AdherenceTimeBand.EVENING,
                    4,
                    6,
                    18,
                    21,
                ),
            concentration =
                listOf(
                    listOf(0.27f, 0.495f, 0.72f, 0.42f, 0.345f, 0.795f, 0.645f),
                    listOf(0.27f, 0.42f, 0.645f, 0.57f, 0.345f, 0.72f, 0.795f),
                    listOf(0.195f, 0.345f, 0.57f, 0.495f, 0.27f, 0.645f, 0.72f),
                ),
            recommendations =
                listOf(
                    AdherenceRecommendation.ADJUST_REMINDER,
                    AdherenceRecommendation.REVIEW_SCHEDULE,
                    AdherenceRecommendation.FOLLOW_UP_ONE_WEEK,
                ),
        )
}
