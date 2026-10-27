package com.vitahealth.tata.analytics.application

import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.ports.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQuery
import com.vitahealth.tata.analytics.application.queries.GetAdherenceSummaryQueryHandler
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards query ownership without a backend or a UI runtime. */
class AnalyticsOwnerValidationTest {

    private class CountingProjections :
        AdherenceSummaryRepository, AdherenceRecommendationsRepository {
        var reads = 0

        override suspend fun getSummary(
            olderAdultId: String,
            periodDays: Int,
        ): AppResult<AdherenceSummaryReadModel?> {
            reads++
            return AppResult.Success(null)
        }

        override suspend fun getRecommendations(
            olderAdultId: String,
            periodDays: Int,
        ): AppResult<AdherenceRecommendationsReadModel?> {
            reads++
            return AppResult.Success(null)
        }
    }

    @Test
    fun emptyAdultReferenceCannotReadSummaryOrInsights() = runBlocking {
        val repository = CountingProjections()
        val summary =
            GetAdherenceSummaryQueryHandler(repository)(
                GetAdherenceSummaryQuery(" ", periodDays = 7)
            )
        val recommendations =
            GetAdherenceRecommendationsQueryHandler(repository)(
                GetAdherenceRecommendationsQuery("", periodDays = 30)
            )

        assertEquals("INVALID_OLDER_ADULT_REFERENCE", (summary as AppResult.Failure).code)
        assertEquals("INVALID_OLDER_ADULT_REFERENCE", (recommendations as AppResult.Failure).code)
        assertEquals(0, repository.reads)
    }

    @Test
    fun validOwnerQueriesCanReturnInsufficientEvidenceWithoutFakeRecords() = runBlocking {
        val repository = CountingProjections()
        val summary =
            GetAdherenceSummaryQueryHandler(repository)(GetAdherenceSummaryQuery("adult", 7))
        val recommendations =
            GetAdherenceRecommendationsQueryHandler(repository)(
                GetAdherenceRecommendationsQuery("adult", 30)
            )

        assertTrue(summary is AppResult.Success)
        assertTrue(recommendations is AppResult.Success)
        assertEquals(2, repository.reads)
    }
}
