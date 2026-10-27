package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MyMedicationsViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import com.vitahealth.tata.treatment.application.queries.GetMyMedicationsOverviewQueryHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyMedicationsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun catalogRemainsAvailableWhenThereIsNoNextIntake() =
        runTest(dispatcher) {
            val medication =
                MyMedication("med", "Medication", "Tablet", true, "1 tablet", "", listOf("08:00"))
            val repository =
                object : MyMedicationsRepository {
                    override suspend fun list() = AppResult.Success(listOf(medication))
                }
            val model =
                MyMedicationsViewModel(
                    GetMyMedicationsOverviewQueryHandler(repository) { AppResult.Success(null) }
                )
            advanceUntilIdle()
            assertEquals(listOf(medication), model.state.value.medications)
            assertNull(model.state.value.nextDose)
            assertFalse(model.state.value.loading)
            model.selectHistory(true)
            assertTrue(model.state.value.history)
        }

    @Test
    fun sessionFailureDoesNotRequestIntakesOrProduceFakeCatalogData() =
        runTest(dispatcher) {
            val repository =
                object : MyMedicationsRepository {
                    override suspend fun list() = AppResult.Failure("", code = "SESSION_REQUIRED")
                }
            val model =
                MyMedicationsViewModel(
                    GetMyMedicationsOverviewQueryHandler(repository) {
                        error("Must not request intakes")
                    }
                )
            advanceUntilIdle()
            assertEquals("SESSION_REQUIRED", model.state.value.errorCode)
            assertFalse(model.state.value.loading)
            assertTrue(model.state.value.medications.isEmpty())
        }

    @Test
    fun nextIntakeFailureIsNotPresentedAsAnEmptyAgenda() =
        runTest(dispatcher) {
            val repository =
                object : MyMedicationsRepository {
                    override suspend fun list() = AppResult.Success(emptyList<MyMedication>())
                }
            val model =
                MyMedicationsViewModel(
                    GetMyMedicationsOverviewQueryHandler(repository) {
                        AppResult.Failure("", code = "SESSION_CHANGED")
                    }
                )
            advanceUntilIdle()
            assertEquals("SESSION_CHANGED", model.state.value.errorCode)
            assertFalse(model.state.value.loading)
            assertNull(model.state.value.nextDose)
        }
}
