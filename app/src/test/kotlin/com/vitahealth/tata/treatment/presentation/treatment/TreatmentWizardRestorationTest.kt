package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.SavedStateHandle
import com.vitahealth.tata.treatment.application.commands.*
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.*
import org.junit.Assert.*
import org.junit.Test

class TreatmentWizardRestorationTest {
    private fun dose(saved: SavedStateHandle) =
        TreatmentDoseFrequencyViewModel(
            "caregiver",
            "adult",
            "Rosa",
            "medication",
            "Losartan",
            "treatment",
            "Control",
            SetDoseFrequencyCommandHandler(),
            saved,
        )

    private fun schedule(saved: SavedStateHandle) =
        TreatmentScheduleInstructionsViewModel(
            "caregiver",
            "adult",
            "Rosa",
            "medication",
            "Losartan",
            "treatment",
            "Control",
            "2 tabletas",
            "Diaria",
            SetScheduleInstructionsCommandHandler(),
            saved,
        )

    private fun reminder(saved: SavedStateHandle) =
        TreatmentReminderViewModel(
            "caregiver",
            "adult",
            "Rosa",
            "medication",
            "Losartan",
            "treatment",
            "Control",
            "2 tabletas",
            "Diaria",
            "08:15",
            "Con agua",
            SetReminderPolicyCommandHandler(),
            saved,
        )

    @Test
    fun doseKeepsInputsAndConsumesCompletionWithoutReplayingItAfterRecreation() {
        val saved = SavedStateHandle()
        val model = dose(saved)
        model.onDosageChange("2 tabletas")
        model.onFrequencyChange("Diaria")
        model.continueConfiguration()
        assertNotNull(model.state.value.validatedBasics)
        model.consumeCompletion()
        assertNull(model.state.value.validatedBasics)
        val restored =
            dose(
                SavedStateHandle(
                    mapOf(
                        "dosage" to saved.get<String>("dosage"),
                        "frequency" to saved.get<String>("frequency"),
                    )
                )
            )
        assertEquals("2 tabletas", restored.state.value.dosage)
        assertEquals("Diaria", restored.state.value.frequency)
        assertNull(restored.state.value.validatedBasics)
    }

    @Test
    fun scheduleAndReminderRestoreOnlyInputsAndDoNotRestoreNavigationResults() {
        val saved = SavedStateHandle()
        val model = schedule(saved)
        model.onScheduleChange("08:15,20:15")
        model.onInstructionsChange("Con agua")
        model.continueConfiguration()
        assertNotNull(model.state.value.validatedSchedule)
        model.consumeCompletion()
        assertNull(model.state.value.validatedSchedule)
        val restored =
            schedule(
                SavedStateHandle(
                    mapOf(
                        "scheduleText" to saved.get<String>("scheduleText"),
                        "instructions" to saved.get<String>("instructions"),
                    )
                )
            )
        assertEquals("08:15,20:15", restored.state.value.scheduleText)
        assertEquals("Con agua", restored.state.value.instructions)
        assertNull(restored.state.value.validatedSchedule)
        val policy = reminder(SavedStateHandle(mapOf("followUpDelayMinutes" to "25")))
        assertEquals("25", policy.state.value.followUpDelayMinutes)
        policy.continueConfiguration()
        assertNotNull(policy.state.value.validatedPolicy)
        policy.consumeCompletion()
        assertNull(policy.state.value.validatedPolicy)
        assertEquals("25", policy.state.value.followUpDelayMinutes)
    }

    @Test
    fun restoredEmptyFieldsAreNotOverwrittenByInitialMedicationSuggestions() {
        val model = dose(SavedStateHandle(mapOf("frequency" to "")))
        model.initializeFrequency("Diaria")
        assertEquals("", model.state.value.frequency)
        val instructions = schedule(SavedStateHandle(mapOf("instructions" to "")))
        instructions.initializeInstructions("Con agua")
        assertEquals("", instructions.state.value.instructions)
    }

    @Test
    fun recreatedLaterStepsShareTheDraftWithoutLeakingValidationResults() {
        val draft = SavedStateHandle()
        val firstSchedule = schedule(draft)
        firstSchedule.onScheduleChange("07:30,19:30")
        firstSchedule.onInstructionsChange("Con el desayuno")
        firstSchedule.continueConfiguration()
        firstSchedule.consumeCompletion()
        val firstReminder = reminder(draft)
        firstReminder.onDelayChange("35")
        firstReminder.continueConfiguration()
        firstReminder.consumeCompletion()

        // These models represent entries recreated after backing through earlier steps.
        val recreatedSchedule = schedule(draft)
        val recreatedReminder = reminder(draft)
        assertEquals("07:30,19:30", recreatedSchedule.state.value.scheduleText)
        assertEquals("Con el desayuno", recreatedSchedule.state.value.instructions)
        assertEquals("35", recreatedReminder.state.value.followUpDelayMinutes)
        assertNull(recreatedSchedule.state.value.validatedSchedule)
        assertNull(recreatedReminder.state.value.validatedPolicy)
        assertEquals("", schedule(SavedStateHandle()).state.value.scheduleText)
    }
}
