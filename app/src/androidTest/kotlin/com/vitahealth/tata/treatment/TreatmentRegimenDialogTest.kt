package com.vitahealth.tata.treatment

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.ui.TreatmentRegimenDialog
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TreatmentRegimenDialogTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun immediateAcknowledgementClosesWithoutAnIntermediateLoadingFrame() {
        var dismissals = 0
        var writes = 0
        val state =
            TreatmentLifecycleUiState(
                "caregiver",
                "adult",
                "Rosa",
                "medication",
                "Losartan",
                "treatment",
                "Control",
                "1 tableta",
                "Diaria",
                "08:00",
                "Con agua",
                10,
                status = TreatmentStatus.ACTIVE,
            )
        compose.setContent {
            TreatmentRegimenDialog(state, { dismissals++ }, { _, _, _, _, _ -> writes++ })
        }
        compose.onNodeWithText("Guardar").performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, writes)
            assertEquals(1, dismissals)
        }
    }

    @Test
    fun retainsTypedRegimenOnFailureAndClosesOnlyAfterAcknowledgedSave() {
        val state =
            mutableStateOf(
                TreatmentLifecycleUiState(
                    "caregiver",
                    "adult",
                    "Rosa",
                    "medication",
                    "Losartan",
                    "treatment",
                    "Control",
                    "1 tableta",
                    "Diaria",
                    "08:00",
                    "Con agua",
                    10,
                    status = TreatmentStatus.PAUSED,
                )
            )
        var dismissals = 0
        val submissions = mutableListOf<List<String>>()
        compose.setContent {
            TreatmentRegimenDialog(
                state.value,
                { dismissals++ },
                { dose, frequency, schedule, instructions, delay ->
                    submissions += listOf(dose, frequency, schedule, instructions, delay)
                    state.value = state.value.copy(isLoading = true, errorMessage = null)
                },
            )
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("2 tabletas")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Guardando...").assertIsNotEnabled()
        compose.onNodeWithText("Cancelar").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(
                listOf("2 tabletas", "Diaria", "08:00", "Con agua", "10"),
                submissions.single(),
            )
            assertEquals(0, dismissals)
            state.value = state.value.copy(isLoading = false, errorMessage = "Sin conexion")
        }
        compose.onNodeWithText("Sin conexion").assertExists()
        compose.onNodeWithText("2 tabletas").assertExists()
        compose.runOnIdle { assertEquals(0, dismissals) }
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle { state.value = state.value.copy(isLoading = false, errorMessage = null) }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(2, submissions.size)
            assertEquals(1, dismissals)
        }
    }
}
