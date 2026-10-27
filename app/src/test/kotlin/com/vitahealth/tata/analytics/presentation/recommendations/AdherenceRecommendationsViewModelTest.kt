package com.vitahealth.tata.analytics.presentation.recommendations

import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceInsightPatternReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQueryHandler
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePatternType
import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceTimeBand
import com.vitahealth.tata.analytics.presentation.recommendations.mappers.toUi
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsUiState
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdherenceRecommendationsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nonCooperativeOldResponseCannotReplaceTheRetryResult() {
        lateinit var pending: Continuation<AppResult<AdherenceRecommendationsReadModel?>>
        var calls = 0
        val repository =
            object : AdherenceRecommendationsRepository {
                override suspend fun getRecommendations(
                    olderAdultId: String,
                    periodDays: Int,
                ): AppResult<AdherenceRecommendationsReadModel?> {
                    calls++
                    return if (calls == 1) suspendCoroutine { pending = it }
                    else AppResult.Success(null)
                }
            }
        val model =
            AdherenceRecommendationsViewModel(
                "adult",
                GetAdherenceRecommendationsQueryHandler(repository),
            )
        model.retry()
        pending.resume(AppResult.Failure("Old result", code = "REQUEST_FAILED"))
        assertEquals(AdherenceRecommendationsUiState.InsufficientEvidence, model.state.value)
        assertEquals(2, calls)
    }

    @Test
    fun delayedFailureFromAnOldLoadCannotReplaceTheRetryResult() {
        val first = CompletableDeferred<AppResult<AdherenceRecommendationsReadModel?>>()
        val current =
            AdherenceRecommendationsReadModel(
                30,
                AdherenceInsightPatternReadModel(
                    AdherencePatternType.OMISSION,
                    AdherenceTimeBand.EVENING,
                    4,
                    6,
                    18,
                    21,
                ),
                emptyList(),
                emptyList(),
            )
        var calls = 0
        val repository =
            object : AdherenceRecommendationsRepository {
                override suspend fun getRecommendations(
                    olderAdultId: String,
                    periodDays: Int,
                ): AppResult<AdherenceRecommendationsReadModel?> {
                    calls++
                    return if (calls == 1) first.await() else AppResult.Success(current)
                }
            }
        val model =
            AdherenceRecommendationsViewModel(
                "adult",
                GetAdherenceRecommendationsQueryHandler(repository),
            )

        model.retry()
        first.complete(AppResult.Failure("Old failure", code = "NETWORK_UNAVAILABLE"))

        assertEquals(2, calls)
        assertEquals(AdherenceRecommendationsUiState.Content(current.toUi()), model.state.value)
    }
}
