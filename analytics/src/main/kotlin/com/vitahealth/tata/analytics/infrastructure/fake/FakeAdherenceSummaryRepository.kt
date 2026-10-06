package com.vitahealth.tata.analytics.infrastructure.fake

import com.vitahealth.tata.analytics.application.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceTrendPointReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.delay
import java.time.LocalDate

enum class FakeAdherenceScenario {
    Content,
    NoData,
    Error,
}

/**
 * Datos de ejemplo mientras el backend de analytics no está disponible.
 * Se reemplaza por una implementación remota que cumpla AdherenceSummaryRepository.
 */
class FakeAdherenceSummaryRepository(
    private val scenario: FakeAdherenceScenario = FakeAdherenceScenario.Content,
) : AdherenceSummaryRepository {
    override suspend fun getSummary(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> {
        delay(600)
        return when (scenario) {
            FakeAdherenceScenario.Content -> AppResult.Success(sampleSummary(periodDays))
            FakeAdherenceScenario.NoData -> AppResult.Success(null)
            FakeAdherenceScenario.Error -> AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private fun sampleSummary(periodDays: Int): AdherenceSummaryReadModel {
        val today = LocalDate.now()
        val daysAgo = listOf(30L, 25L, 20L, 15L, 10L, 5L, 0L)
        val percents = listOf(57, 74, 51, 79, 62, 73, 92)
        return AdherenceSummaryReadModel(
            periodDays = periodDays,
            scheduledCount = 120,
            adherencePercent = 92,
            adherenceChangePercent = 8,
            onTimePercent = 86,
            onTimeChangePercent = 12,
            lateCount = 6,
            omittedCount = 4,
            trend = daysAgo.zip(percents) { ago, percent ->
                AdherenceTrendPointReadModel(date = today.minusDays(ago), adherencePercent = percent)
            },
        )
    }
}
