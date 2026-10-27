package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
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
class TreatmentDetailViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun anOlderLoadCannotReplaceTheLatestTreatmentState() {
        val pending = CompletableDeferred<AppResult<TreatmentDetailReadModel>>()
        val current =
            TreatmentDetailReadModel(
                "treatment",
                "adult",
                "Presión",
                TreatmentStatus.PAUSED,
                null,
                null,
                null,
                emptyList(),
                "",
                null,
            )
        var calls = 0
        val repository =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<TreatmentDetailReadModel> {
                    calls++
                    return if (calls == 1) pending.await() else AppResult.Success(current)
                }
            }
        val model =
            TreatmentDetailViewModel(
                "caregiver",
                "Rosa",
                "treatment",
                "Losartán",
                GetTreatmentDetailQueryHandler(repository),
            )

        model.retry()
        pending.complete(AppResult.Success(current.copy(status = TreatmentStatus.ACTIVE)))

        assertEquals(2, calls)
        assertEquals(
            TreatmentStatus.PAUSED,
            (model.state.value as TreatmentDetailUiState.Loaded).detail.status,
        )
    }
}
