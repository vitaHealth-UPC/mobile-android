package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommand
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommand
import com.vitahealth.tata.treatment.application.commands.TreatmentLifecycleAction
import com.vitahealth.tata.treatment.domain.model.Treatment
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TreatmentLifecycleHandlersTest {
    @Test
    fun invalidScheduleIsRejectedBeforeRepositoryCall() = runBlocking {
        val repository = FakeLifecycleRepository()
        val handler = ConfigureTreatmentCommandHandler(repository)

        val result = handler(
            ConfigureTreatmentCommand(
                caregiverId = "caregiver-1",
                treatmentId = "treatment-1",
                medicationId = "med-1",
                dose = "1 comprimido",
                frequency = "Cada día",
                scheduledTimes = listOf("25:99"),
                instructions = "Con agua",
                reminderLeadMinutes = 10,
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_TREATMENT_CONFIGURATION", (result as AppResult.Failure).code)
        assertFalse(repository.configureCalled)
    }

    @Test
    fun validConfigurationIsNormalizedAndForwarded() = runBlocking {
        val repository = FakeLifecycleRepository()
        val handler = ConfigureTreatmentCommandHandler(repository)

        val result = handler(
            ConfigureTreatmentCommand(
                caregiverId = " caregiver-1 ",
                treatmentId = " treatment-1 ",
                medicationId = " med-1 ",
                dose = " 1 comprimido ",
                frequency = " Cada día ",
                scheduledTimes = listOf("08:00", "20:00", "08:00"),
                instructions = " Con agua ",
                reminderLeadMinutes = 10,
            ),
        )

        assertTrue(result is AppResult.Success)
        assertTrue(repository.configureCalled)
        assertEquals(listOf("08:00", "20:00"), repository.scheduledTimes)
        assertEquals("Con agua", repository.instructions)
    }

    @Test
    fun pauseActionDispatchesToPauseEndpoint() = runBlocking {
        val repository = FakeLifecycleRepository()
        val handler = ChangeTreatmentStatusCommandHandler(repository)

        val result = handler(
            ChangeTreatmentStatusCommand(
                caregiverId = "caregiver-1",
                treatmentId = "treatment-1",
                action = TreatmentLifecycleAction.PAUSE,
            ),
        )

        assertTrue(result is AppResult.Success)
        assertEquals(TreatmentStatus.PAUSED, (result as AppResult.Success).value.status)
        assertTrue(repository.pauseCalled)
    }

    private class FakeLifecycleRepository : TreatmentLifecycleRepository {
        var configureCalled = false
        var pauseCalled = false
        var scheduledTimes: List<String>? = null
        var instructions: String? = null

        override suspend fun configureTreatment(
            caregiverId: String,
            treatmentId: String,
            medicationId: String,
            dose: String,
            frequency: String,
            scheduledTimes: List<String>,
            instructions: String,
            reminderLeadMinutes: Int,
        ): AppResult<Treatment> {
            configureCalled = true
            this.scheduledTimes = scheduledTimes
            this.instructions = instructions
            return AppResult.Success(treatment(TreatmentStatus.DRAFT))
        }

        override suspend fun activateTreatment(
            caregiverId: String,
            treatmentId: String,
        ): AppResult<Treatment> = AppResult.Success(treatment(TreatmentStatus.ACTIVE))

        override suspend fun pauseTreatment(
            caregiverId: String,
            treatmentId: String,
        ): AppResult<Treatment> {
            pauseCalled = true
            return AppResult.Success(treatment(TreatmentStatus.PAUSED))
        }

        override suspend fun resumeTreatment(
            caregiverId: String,
            treatmentId: String,
        ): AppResult<Treatment> = AppResult.Success(treatment(TreatmentStatus.ACTIVE))

        private fun treatment(status: TreatmentStatus) = Treatment(
            id = "treatment-1",
            olderAdultId = "adult-1",
            name = "Control de presión",
            status = status,
        )
    }
}
