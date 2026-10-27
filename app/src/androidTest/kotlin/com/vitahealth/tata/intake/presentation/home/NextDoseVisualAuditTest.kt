package com.vitahealth.tata.intake.presentation.home

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.agenda.screens.IntakeAgendaScreen
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaUiState
import com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen
import com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
import com.vitahealth.tata.intake.presentation.home.screens.NextDoseHomeScreen
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NextDoseVisualAuditTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun captureExistingStatesAndVerifyTheirNavigation() {
        val dose =
            NextDoseReadModel(
                "dose-test",
                "treatment-test",
                "med-test",
                "adult-test",
                "Losartán 50 mg",
                "1 tableta",
                "Tomar con agua",
                Instant.now().plusSeconds(1500),
                DoseStatus.PENDING,
            )
        var state by
            mutableStateOf<NextDoseHomeUiState>(
                NextDoseHomeUiState.NextDoseAvailable(dose, "Rosa Vargas", DailyDoseProgress(2, 3))
            )
        var opened: String? = null
        var agenda = 0
        var retried = 0
        var voiceOpened: String? = null
        compose.setContent {
            ProductionSpanishTheme {
                NextDoseHomeScreen(
                    state,
                    onRetry = { retried++ },
                    onOpenDoseDetail = { opened = it },
                    onOpenAgenda = { agenda++ },
                    onOpenVoice = { voiceOpened = it },
                    modifier = Modifier,
                )
            }
        }
        capture("home-existing-dose")
        compose.onNodeWithText("¡Vas muy bien!").assertExists()
        compose.onNodeWithText("Inicio").assertIsSelected()
        compose.onNodeWithText("Medicamentos").assertIsNotEnabled()
        compose.onNodeWithText("Notas").assertIsNotEnabled()
        compose.onNodeWithText("Más").assertIsNotEnabled()
        compose.onNodeWithText("Agenda").performClick()
        compose.onNodeWithText("Losartán 50 mg").performClick()
        compose.runOnIdle { assertEquals("dose-test", opened) }
        compose.onNodeWithText("Ver todas\nmis tomas").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(2, agenda)
            state = NextDoseHomeUiState.NoNextDose("Rosa Vargas")
        }
        compose.onNodeWithText("Hoy").performScrollTo()
        compose.onNodeWithText("Sin próxima toma").assertExists()
        compose.onNodeWithText("Losartán 50 mg").assertDoesNotExist()
        compose.onNodeWithText("Agenda al día").assertExists()
        compose.onNodeWithText("No existen próximas tomas pendientes.").assertExists()
        compose.onNodeWithText("Confirmar\ncon voz").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(null, voiceOpened) }
        compose.onNodeWithText("Ver todas\nmis tomas").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(3, agenda) }
        compose.onNodeWithText("Hoy").performScrollTo()
        capture("home-existing-empty")
        compose.runOnIdle { state = NextDoseHomeUiState.Error("Rosa Vargas", "Sin conexión") }
        compose.onNodeWithText("Reintentar").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, retried) }
        capture("home-existing-error")
    }

    @Test
    fun captureDoseStatusesAndConfirmationOutcome() {
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "dose-test",
                "treatment-test",
                "med-test",
                "adult-test",
                "Losartán 50 mg",
                "1 tableta",
                "Tomar con agua",
                Instant.now(),
                DoseStatus.PENDING,
            )
        var state by
            mutableStateOf(
                com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState.Content(
                    dose
                )
            )
        var confirmations = 0
        compose.setContent {
            ProductionSpanishTheme {
                com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen(
                    state,
                    onBack = {},
                    onRetry = {},
                    onConfirm = { confirmations++ },
                    modifier = Modifier,
                )
            }
        }
        for (status in DoseStatus.entries) {
            compose.runOnIdle { state = state.copy(dose = dose.copy(status = status)) }
            capture("dose-existing-${status.name.lowercase()}")
            if (status == DoseStatus.PENDING)
                compose.onNodeWithText("Confirmar toma").assertExists()
            else compose.onNodeWithText("Confirmar toma").assertDoesNotExist()
        }
        compose.runOnIdle { state = state.copy(dose = dose, confirming = true) }
        compose.onNodeWithText("Confirmando...").assertIsNotEnabled()
        compose.runOnIdle { state = state.copy(confirming = false) }
        compose.onNodeWithText("Confirmar toma").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, confirmations)
            state = state.copy(confirmationSucceeded = true)
        }
        capture("dose-existing-success")
        compose.onNodeWithText("Volver al inicio").assertExists()
        compose.runOnIdle {
            state =
                state.copy(
                    dose = dose.copy(status = DoseStatus.LATE, confirmedAt = Instant.now()),
                    confirmationSucceeded = true,
                    outcome =
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
                            .LATE,
                )
        }
        capture("dose-existing-late-success")
        compose.runOnIdle {
            state =
                state.copy(
                    dose = dose.copy(status = DoseStatus.OMITTED),
                    confirmationSucceeded = false,
                    confirmationMessage = "El periodo de confirmación finalizó.",
                    outcome =
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
                            .OMISSION_PRESERVED,
                )
        }
        capture("dose-existing-omission-preserved")
        compose.onNodeWithText("Confirmar toma").assertDoesNotExist()
    }

    @Test
    fun captureAgendaAndVerifyWeekNavigation() {
        val day = java.time.LocalDate.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "dose-test",
                "treatment-test",
                "med-test",
                "adult-test",
                "Losartán 50 mg",
                "1 comprimido",
                "Con el desayuno",
                day.atTime(8, 0).atZone(java.time.ZoneId.systemDefault()).toInstant(),
                DoseStatus.PENDING,
            )
        var moves = 0L
        var opened: String? = null
        var doses by mutableStateOf(listOf(dose))
        compose.setContent {
            ProductionSpanishTheme {
                androidx.compose.foundation.layout.Box(Modifier) {
                    com.vitahealth.tata.intake.presentation.agenda.screens.IntakeAgendaScreen(
                        com.vitahealth.tata.intake.presentation.agenda.viewmodel
                            .IntakeAgendaUiState(selectedDay = day, doses = doses, loading = false),
                        onSelectDay = {},
                        onMoveWeek = { moves = it },
                        onRetry = {},
                        onOpenDose = { opened = it },
                        onHome = {},
                    )
                }
            }
        }
        capture("agenda-existing")
        compose.onNodeWithText("Losartán 50 mg").performClick()
        compose.runOnIdle { assertEquals("dose-test", opened) }
        compose.onNodeWithContentDescription("Siguiente").performClick()
        compose.runOnIdle { assertEquals(1L, moves) }
        compose.runOnIdle {
            doses =
                DoseStatus.entries.mapIndexed { index, status ->
                    dose.copy(
                        id = "dose-$index",
                        status = status,
                        medicationName =
                            listOf("Losartán", "Vitamina D3", "Amlodipino", "Atorvastatina")[index],
                        scheduledAt =
                            day.atTime(8 + index * 3, 0)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant(),
                    )
                }
        }
        capture("agenda-existing-statuses")
    }

    /**
     * Captures the Compose hierarchy without unrelated launcher or System UI dialogs. Files appear
     * under Pictures/TataAudit and can be pulled from the emulator.
     */
    private fun capture(name: String) {
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "clean-$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
            }
        val image =
            requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        resolver.openOutputStream(image)!!.use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}

/**
 * The instrumentation host is a generic Compose activity. Production MainActivity supplies an
 * es-419 configuration context; mirror it here so assertions and Figma screenshots test the actual
 * product language.
 */
@Composable
private fun ProductionSpanishTheme(content: @Composable () -> Unit) {
    val original = LocalContext.current
    val spanish = remember(original) { original.withSpanishAppLocale() }
    CompositionLocalProvider(
        LocalContext provides spanish,
        LocalConfiguration provides spanish.resources.configuration,
    ) {
        TataTheme { content() }
    }
}
