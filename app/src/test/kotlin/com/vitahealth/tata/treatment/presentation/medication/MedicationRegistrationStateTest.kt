package com.vitahealth.tata.treatment.presentation.medication

import androidx.lifecycle.SavedStateHandle
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.application.handlers.RegisterMedicationCommandHandler
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.Treatment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationRegistrationStateTest {
    private val repository = object : TreatmentRepository {
        override suspend fun registerMedication(caregiverId: String, olderAdultId: String,
            name: String, presentation: String): AppResult<Medication> = error("Restoration must not register")
        override suspend fun createTreatment(caregiverId: String, olderAdultId: String,
            name: String): AppResult<Treatment> = error("Restoration must not create a treatment")
    }

    @Test
    fun recreatesEditableDraftWithoutReplayingRegistration() {
        val saved = SavedStateHandle()
        val first = model(saved)
        first.onNameChange("Losartán")
        first.onPresentationChange("50 mg")
        first.onFrequencyChange("Cada 12 horas")
        first.onTimingChange("Con desayuno")
        first.onNotesChange("Con agua")
        val restoredHandle = SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) })
        val restored = model(restoredHandle).state.value
        assertEquals(first.state.value, restored)
        assertFalse(restored.isLoading)
        assertNull(restored.registeredMedication)
    }

    private fun model(handle: SavedStateHandle) = MedicationRegistrationViewModel(
        "caregiver-1", "adult-1", "Rosa", RegisterMedicationCommandHandler(repository), handle,
    )
}
