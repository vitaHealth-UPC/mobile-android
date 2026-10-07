package com.vitahealth.tata.analytics.infrastructure.fake

import com.vitahealth.tata.analytics.application.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceTrendPointReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecentIntakeReadModel
import com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

enum class FakeAdherenceScenario {
    Content,
    NoData,

    /** Hay datos para 30 días pero ninguno para 7 días: permite ver "Sin resultados". */
    EmptyWeek,
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
        delay(600.milliseconds)
        return when (scenario) {
            FakeAdherenceScenario.Content -> AppResult.Success(sampleSummary(periodDays))
            FakeAdherenceScenario.NoData -> AppResult.Success(null)
            FakeAdherenceScenario.EmptyWeek -> AppResult.Success(
                if (periodDays <= WEEK_DAYS) null else sampleSummary(periodDays),
            )
            FakeAdherenceScenario.Error -> AppResult.Failure(
                message = "No hay conexión. Inténtalo nuevamente.",
                code = "NETWORK_UNAVAILABLE",
            )
        }
    }

    private fun sampleSummary(periodDays: Int): AdherenceSummaryReadModel {
        val today = LocalDate.now()
        val isWeek = periodDays <= WEEK_DAYS
        val daysAgo = if (isWeek) listOf(6L, 5L, 4L, 3L, 2L, 1L, 0L) else listOf(30L, 25L, 20L, 15L, 10L, 5L, 0L)
        val percents = if (isWeek) listOf(88, 90, 85, 95, 92, 94, 92) else listOf(57, 74, 51, 79, 62, 73, 92)
        return AdherenceSummaryReadModel(
            periodDays = periodDays,
            scheduledCount = if (isWeek) 28 else 120,
            adherencePercent = 92,
            adherenceChangePercent = if (isWeek) 4 else 8,
            onTimePercent = if (isWeek) 88 else 86,
            onTimeChangePercent = if (isWeek) 5 else 12,
            lateCount = if (isWeek) 3 else 6,
            omittedCount = if (isWeek) 1 else 4,
            trend = daysAgo.zip(percents) { ago, percent ->
                AdherenceTrendPointReadModel(date = today.minusDays(ago), adherencePercent = percent)
            },
            recentIntakes = listOf(
                RecentIntakeReadModel(
                    scheduledAt = today.atTime(8, 0),
                    medicationName = "Losartán",
                    status = IntakeOutcomeStatus.OnTime,
                    minutesLate = null,
                ),
                RecentIntakeReadModel(
                    scheduledAt = today.minusDays(1).atTime(20, 0),
                    medicationName = "Amlodipino",
                    status = IntakeOutcomeStatus.Late,
                    minutesLate = 24,
                ),
                RecentIntakeReadModel(
                    scheduledAt = today.minusDays(3).atTime(13, 0),
                    medicationName = "Vitamina D3",
                    status = IntakeOutcomeStatus.Omitted,
                    minutesLate = null,
                ),
            ),
        )
    }

    private companion object {
        const val WEEK_DAYS = 7
    }
}
