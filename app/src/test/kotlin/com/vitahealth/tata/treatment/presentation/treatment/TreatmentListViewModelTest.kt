package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListViewModel
import androidx.lifecycle.ViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentCatalogRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQueryHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TreatmentListViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun supersededNonCooperativeLoadCannotPublishAnError() {
        lateinit var first: Continuation<AppResult<List<TreatmentDetailReadModel>>>
        var calls = 0
        val repository =
            object : TreatmentCatalogRepository {
                override suspend fun listTreatments(
                    caregiverId: String,
                    olderAdultId: String,
                ): AppResult<List<TreatmentDetailReadModel>> {
                    calls++
                    return if (calls == 1) suspendCoroutine { first = it }
                    else AppResult.Success(emptyList())
                }
            }
        val model =
            TreatmentListViewModel("caregiver", "adult", ListTreatmentsQueryHandler(repository))
        model.refresh()
        first.resume(AppResult.Failure("Old failure", code = "REQUEST_FAILED"))

        assertEquals(2, calls)
        assertEquals(false, model.state.value.loading)
        assertNull(model.state.value.error)
    }

    @Test
    fun factoryRejectsUnrelatedViewModelBeforeStartingARequest() {
        var calls = 0
        val repository =
            object : TreatmentCatalogRepository {
                override suspend fun listTreatments(
                    caregiverId: String,
                    olderAdultId: String,
                ): AppResult<List<TreatmentDetailReadModel>> {
                    calls++
                    return AppResult.Success(emptyList())
                }
            }
        val factory =
            TreatmentListViewModel.Factory(
                "caregiver",
                "adult",
                ListTreatmentsQueryHandler(repository),
            )
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnrelatedViewModel::class.java)
        }
        assertEquals(0, calls)
    }

    private class UnrelatedViewModel : ViewModel()
}
