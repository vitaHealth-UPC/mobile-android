package com.vitahealth.tata.intake.presentation.home

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.design.theme.TataTheme
import java.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class NextDoseVisualAuditTest {
    @get:Rule val compose = createComposeRule()
    @Test fun captureExistingStatesAndVerifyTheirNavigation() {
        val dose = NextDoseReadModel("dose-test", "treatment-test", "med-test", "adult-test",
            "Losartán 50 mg", "1 tableta", "Tomar con agua", Instant.now().plusSeconds(1500), DoseStatus.PENDING)
        var state by mutableStateOf<NextDoseHomeUiState>(NextDoseHomeUiState.NextDoseAvailable(dose, "Rosa Vargas", DailyDoseProgress(2, 3)))
        var opened: String? = null
        var agenda = 0
        var retried = 0
        compose.setContent {
            TataTheme {
                NextDoseHomeScreen(state, onRetry = { retried++ }, onOpenDoseDetail = { opened = it },
                    onOpenAgenda = { agenda++ }, modifier = Modifier.safeDrawingPadding())
            }
        }
        capture("home-existing-dose")
        compose.onNodeWithText("Inicio").assertIsSelected()
        compose.onNodeWithText("Medicamentos").assertIsNotEnabled()
        compose.onNodeWithText("Notas").assertIsNotEnabled()
        compose.onNodeWithText("Más").assertIsNotEnabled()
        compose.onNodeWithText("Agenda").performClick()
        compose.onNodeWithText("Losartán 50 mg").performClick()
        compose.runOnIdle { assertEquals("dose-test", opened) }
        compose.onNodeWithText("Ver todas\nmis tomas").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(2, agenda); state = NextDoseHomeUiState.NoNextDose("Rosa Vargas") }
        compose.onNodeWithText("Hoy").performScrollTo()
        compose.onNodeWithText("Sin próxima toma").assertExists()
        capture("home-existing-empty")
        compose.runOnIdle { state = NextDoseHomeUiState.Error("Rosa Vargas", "Sin conexión") }
        compose.onNodeWithText("Reintentar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retried) }
        capture("home-existing-error")
    }
    @Test fun captureDoseStatusesAndConfirmationOutcome() {
        val dose = com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("dose-test", "treatment-test", "med-test", "adult-test",
            "Losartán 50 mg", "1 tableta", "Tomar con agua", Instant.now(), DoseStatus.PENDING)
        var state by mutableStateOf(com.vitahealth.tata.intake.presentation.detail.DoseDetailUiState.Content(dose))
        var confirmations = 0
        compose.setContent {
            TataTheme {
                com.vitahealth.tata.intake.presentation.detail.DoseDetailScreen(state, onBack = {}, onRetry = {},
                    onConfirm = { confirmations++ }, modifier = Modifier.safeDrawingPadding())
            }
        }
        for (status in DoseStatus.entries) {
            compose.runOnIdle { state = state.copy(dose = dose.copy(status = status)) }
            capture("dose-existing-${status.name.lowercase()}")
            if (status == DoseStatus.PENDING) compose.onNodeWithText("Confirmar toma").assertExists()
            else compose.onNodeWithText("Confirmar toma").assertDoesNotExist()
        }
        compose.runOnIdle { state = state.copy(dose = dose, confirming = true) }
        compose.onNodeWithText("Confirmando...").assertIsNotEnabled()
        compose.runOnIdle { state = state.copy(confirming = false) }
        compose.onNodeWithText("Confirmar toma").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, confirmations); state = state.copy(confirmationSucceeded = true) }
        capture("dose-existing-success")
        compose.onNodeWithText("Volver al inicio").assertExists()
        compose.runOnIdle { state = state.copy(dose = dose.copy(status = DoseStatus.LATE, confirmedAt = Instant.now()), confirmationSucceeded = true, outcome = com.vitahealth.tata.intake.presentation.detail.ConfirmationOutcome.LATE) }
        capture("dose-existing-late-success")
        compose.runOnIdle { state = state.copy(dose = dose.copy(status = DoseStatus.OMITTED), confirmationSucceeded = false,
            confirmationMessage = "El periodo de confirmación finalizó.", outcome = com.vitahealth.tata.intake.presentation.detail.ConfirmationOutcome.OMISSION_PRESERVED) }
        capture("dose-existing-omission-preserved")
        compose.onNodeWithText("Confirmar toma").assertDoesNotExist()
    }
    @Test fun captureAgendaAndVerifyWeekNavigation() {
        val day = java.time.LocalDate.now()
        val dose = com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("dose-test", "treatment-test", "med-test", "adult-test",
            "Losartán 50 mg", "1 comprimido", "Con el desayuno", day.atTime(8,0).atZone(java.time.ZoneId.systemDefault()).toInstant(), DoseStatus.PENDING)
        var moves = 0L
        var opened: String? = null
        var doses by mutableStateOf(listOf(dose))
        compose.setContent {
            TataTheme {
                androidx.compose.foundation.layout.Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaScreen(
                        com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaUiState(selectedDay = day, doses = doses, loading = false),
                        onSelectDay = {}, onMoveWeek = { moves = it }, onRetry = {}, onOpenDose = { opened = it }, onHome = {})
                }
            }
        }
        capture("agenda-existing")
        compose.onNodeWithText("Losartán 50 mg").performClick()
        compose.runOnIdle { assertEquals("dose-test", opened) }
        compose.onNodeWithText("Siguiente").performClick()
        compose.runOnIdle { assertEquals(1L, moves) }
        compose.runOnIdle { doses = DoseStatus.entries.mapIndexed { index, status -> dose.copy(id = "dose-$index", status = status,
            medicationName = listOf("Losartán", "Vitamina D3", "Amlodipino", "Atorvastatina")[index],
            scheduledAt = day.atTime(8 + index * 3, 0).atZone(java.time.ZoneId.systemDefault()).toInstant()) } }
        capture("agenda-existing-statuses")
    }
    private fun capture(name: String) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(500)
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("screencap -p /data/local/tmp/$name.png")).use { it.readBytes() }
    }
}
