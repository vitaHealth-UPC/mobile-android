package com.vitahealth.tata.analytics.application

import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.ports.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsPeriodValidationTest {
    @Test
    fun periodsOutsideTheBackendRangeDoNotReachEitherRepository() = runBlocking {
        val repository = RecordingRepository()
        for (days in listOf(-1, 0, 32, Int.MAX_VALUE)) {
            val summary =
                GetAdherenceSummaryQueryHandler(repository)(GetAdherenceSummaryQuery("adult", days))
            val insights =
                GetAdherenceRecommendationsQueryHandler(repository)(
                    GetAdherenceRecommendationsQuery("adult", days)
                )
            assertEquals("INVALID_PERIOD", (summary as AppResult.Failure).code)
            assertEquals("INVALID_PERIOD", (insights as AppResult.Failure).code)
        }
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun rangeBoundariesAndCurrentScreenPeriodsAreAccepted() = runBlocking {
        val repository = RecordingRepository()
        for (days in listOf(1, 7, 30, 31)) {
            assertTrue(
                GetAdherenceSummaryQueryHandler(repository)(
                    GetAdherenceSummaryQuery(" adult ", days)
                )
                    is AppResult.Success
            )
            assertTrue(
                GetAdherenceRecommendationsQueryHandler(repository)(
                    GetAdherenceRecommendationsQuery(" adult ", days)
                )
                    is AppResult.Success
            )
        }
        assertEquals(listOf(1, 1, 7, 7, 30, 30, 31, 31), repository.calls.map { it.second })
        assertTrue(repository.calls.all { it.first == "adult" })
    }

    private class RecordingRepository :
        AdherenceSummaryRepository, AdherenceRecommendationsRepository {
        val calls = mutableListOf<Pair<String, Int>>()

        override suspend fun getSummary(
            olderAdultId: String,
            periodDays: Int,
        ): AppResult<AdherenceSummaryReadModel?> {
            calls += olderAdultId to periodDays
            return AppResult.Success(null)
        }

        override suspend fun getRecommendations(
            olderAdultId: String,
            periodDays: Int,
        ): AppResult<AdherenceRecommendationsReadModel?> {
            calls += olderAdultId to periodDays
            return AppResult.Success(null)
        }
    }
}
