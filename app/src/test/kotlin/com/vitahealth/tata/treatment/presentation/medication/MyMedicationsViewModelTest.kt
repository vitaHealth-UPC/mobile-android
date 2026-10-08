package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.handlers.GetMyMedicationsQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MyMedicationsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun catalogRemainsAvailableWhenThereIsNoNextIntake() = runTest(dispatcher) {
        val medication = MyMedication("med", "Medication", "Tablet", true, "1 tablet", "", listOf("08:00"))
        val repository = object : MyMedicationsRepository {
            override suspend fun list() = AppResult.Success(listOf(medication))
        }
        val model = MyMedicationsViewModel(GetMyMedicationsQueryHandler(repository)) { null }
        advanceUntilIdle()
        assertEquals(listOf(medication), model.state.value.medications)
        assertNull(model.state.value.nextDose)
        assertFalse(model.state.value.loading)
        model.selectHistory(true)
        assertTrue(model.state.value.history)
    }

    @Test fun sessionFailureDoesNotRequestIntakesOrProduceFakeCatalogData() = runTest(dispatcher) {
        val repository = object : MyMedicationsRepository {
            override suspend fun list() = AppResult.Failure("", code = "SESSION_REQUIRED")
        }
        val model = MyMedicationsViewModel(GetMyMedicationsQueryHandler(repository)) { error("Must not request intakes") }
        advanceUntilIdle()
        assertEquals("SESSION_REQUIRED", model.state.value.errorCode)
        assertFalse(model.state.value.loading)
        assertTrue(model.state.value.medications.isEmpty())
    }
}
