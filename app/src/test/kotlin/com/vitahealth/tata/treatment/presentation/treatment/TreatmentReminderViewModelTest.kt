package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentReminderViewModel
import androidx.lifecycle.ViewModel
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommandHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TreatmentReminderViewModelTest {
    @Test
    fun factoryAcceptsItsModelAndRejectsAnUnrelatedType() {
        val factory =
            TreatmentReminderViewModel.Factory(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartán",
                "treatment",
                "Presión",
                "1 comprimido",
                "Diaria",
                "08:00",
                "",
                SetReminderPolicyCommandHandler(),
            )
        assertThrows(IllegalArgumentException::class.java) {
            factory.create(UnrelatedViewModel::class.java)
        }
        assertTrue(
            factory.create(TreatmentReminderViewModel::class.java) is TreatmentReminderViewModel
        )
    }

    private class UnrelatedViewModel : ViewModel()

    private fun model() =
        TreatmentReminderViewModel(
            "caregiver",
            "adult",
            "Rosa",
            "medication",
            "Losartán",
            "treatment",
            "Presión",
            "1 comprimido",
            "Diaria",
            "08:00",
            "",
            SetReminderPolicyCommandHandler(),
        )

    @Test
    fun invalidInputIsRejectedWithoutBeingConvertedToAnotherDelay() {
        val model = model()
        for (input in listOf("-10", "1.5", "12x", "10000", "")) {
            model.onDelayChange(input)
            assertEquals(input, model.state.value.followUpDelayMinutes)
            model.continueConfiguration()
            assertNull(model.state.value.validatedPolicy)
            assertNotNull(model.state.value.errorMessage)
        }
    }

    @Test
    fun editingInvalidatesThePreviouslyValidatedDelay() {
        val model = model()
        model.onDelayChange(" 15 ")
        model.continueConfiguration()
        assertEquals(15, model.state.value.validatedPolicy?.followUpDelayMinutes?.value)
        model.onDelayChange("-15")
        assertNull(model.state.value.validatedPolicy)
        model.continueConfiguration()
        assertNotNull(model.state.value.errorMessage)
    }
}
