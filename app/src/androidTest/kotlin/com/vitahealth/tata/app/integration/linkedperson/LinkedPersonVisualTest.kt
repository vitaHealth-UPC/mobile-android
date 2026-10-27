package com.vitahealth.tata.app.integration.linkedperson

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.carelink.application.commands.UpdateLinkedPersonProfileCommandHandler
import com.vitahealth.tata.carelink.application.ports.LinkedPersonProfileUpdater
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkedPersonVisualTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun treatment(id: String, name: String) =
        TreatmentDetailReadModel(
            id = id,
            olderAdultId = "adult-1",
            name = name,
            status = TreatmentStatus.ACTIVE,
            medicationId = id,
            dose = null,
            frequency = null,
            scheduledTimes = emptyList(),
            instructions = "",
            reminderLeadMinutes = null,
        )

    private val fixture =
        LinkedPersonSnapshot(
            MonitoredPerson("adult-1", "Rosa Vargas", LocalDate.of(1958, 5, 12)),
            FamilySummary(
                "Rosa Vargas",
                LocalDate.of(2026, 10, 26),
                AdherenceCounts(23, 25),
                AdherenceCounts(2, 2),
                null,
                emptyList(),
                emptyList(),
                true,
            ),
            listOf(
                treatment("a", "Losartán 50 mg"),
                treatment("b", "Metformina 500 mg"),
                treatment("c", "Levotiroxina 75 mcg"),
            ),
        )

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(750)
        val frame = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val output =
            requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)).resolve(name)
        output.outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
        frame.recycle()
    }

    @Test
    fun emergencyContactFormSavesOnceAndCancelDoesNotWrite() {
        var writes = 0
        val factory =
            LinkedPersonViewModel.Factory(
                "caregiver-1",
                "adult-1",
                "Rosa Vargas",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(fixture) },
                ContactOptionReader { AppResult.Success(ContactOption(null, "Sin contacto")) },
                UpdateLinkedPersonProfileCommandHandler(
                    LinkedPersonProfileUpdater { _, _, _, _ -> AppResult.Success(Unit) }
                ),
                com.vitahealth.tata.carelink.application.commands
                    .UpdateLinkedPersonEmergencyContactCommandHandler(
                        com.vitahealth.tata.carelink.application.ports
                            .LinkedPersonEmergencyContactUpdater { _, _, name, relationship, phone
                                ->
                                assertEquals("Ana", name)
                                assertEquals("Hija", relationship)
                                assertEquals("+51912345678", phone)
                                writes++
                                AppResult.Success(Unit)
                            }
                    ),
            )
        compose.setContent { TataTheme { LinkedPersonRoute(factory, {}, {}, {}, {}, {}) } }
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("Editar contacto de emergencia").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle { assertEquals(0, writes) }
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("Editar contacto de emergencia").performClick()
        compose.onNodeWithTag("linked-contact-name").performTextReplacement("Ana")
        compose.onNodeWithTag("linked-contact-relationship").performTextReplacement("Hija")
        compose.onNodeWithTag("linked-contact-phone").performTextReplacement("+51912345678")
        capture("tata-linked-emergency-contact-20261026.png")
        compose.onNodeWithText("Guardar").performClick()
        compose.waitForIdle()
        compose.runOnIdle { assertEquals(1, writes) }
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
    }

    @Test
    fun linkedPersonContentAndCallbacksAreReal() {
        var medication = 0
        var history = 0
        compose.setContent {
            TataTheme {
                LinkedPersonScreen(
                    state = LinkedPersonUiState(loading = false, snapshot = fixture),
                    onBack = {},
                    onRetry = {},
                    onCall = {},
                    onMessage = {},
                    onMedication = { medication++ },
                    onHistory = { history++ },
                    onTreatments = {},
                    onTabSelected = { _: CaregiverTab -> },
                )
            }
        }
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
        compose.onNodeWithText("68 años").assertIsDisplayed()
        compose.onNodeWithText("92%").assertIsDisplayed()
        compose.onNodeWithText("3 tratamientos activos").assertIsDisplayed()
        compose.waitForIdle()
        compose.onNodeWithText("Ver\nmedicación").performScrollTo().performClick()
        compose.onNodeWithText("Historial").performScrollTo()
        capture("tata-linked16-stabilized-20261026.png")
        compose.onNodeWithText("Historial").performClick()
        compose.waitForIdle()
        assertEquals(1, medication)
        assertEquals(1, history)
    }

    @Test
    fun openingAndSavingProfileDispatchesOneVerifiedWrite() {
        var latest = fixture
        var writes = 0
        val factory =
            LinkedPersonViewModel.Factory(
                "caregiver-1",
                "adult-1",
                "Rosa Vargas",
                LinkedPersonSnapshotReader { _, _, _ -> AppResult.Success(latest) },
                ContactOptionReader { AppResult.Success(ContactOption(null, null)) },
                UpdateLinkedPersonProfileCommandHandler(
                    LinkedPersonProfileUpdater { _, adult, name, birthDate ->
                        org.junit.Assert.assertEquals("adult-1", adult)
                        writes++
                        latest =
                            latest.copy(
                                person = latest.person.copy(fullName = name, birthDate = birthDate)
                            )
                        AppResult.Success(Unit)
                    }
                ),
            )
        compose.setContent {
            TataTheme {
                LinkedPersonRoute(
                    factory,
                    onBack = {},
                    onMedication = {},
                    onHistory = {},
                    onTreatments = {},
                    onTabSelected = {},
                )
            }
        }
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("Editar perfil vinculado").assertIsDisplayed()
        compose.onNodeWithTag("linked-person-name").performTextReplacement("Rosa Verificada")
        compose.onNodeWithText("Guardar").performClick()
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Rosa Verificada").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Rosa Verificada").assertIsDisplayed()
        assertEquals(1, writes)
    }

    @Test
    fun revokedConsentDisplaysNoProtectedSnapshot() {
        compose.setContent {
            TataTheme {
                LinkedPersonScreen(
                    state =
                        LinkedPersonUiState(
                            loading = false,
                            snapshot = null,
                            accessDenied = true,
                            error = "El vínculo fue revocado.",
                        ),
                    onBack = {},
                    onRetry = {},
                    onCall = {},
                    onMessage = {},
                    onMedication = {},
                    onHistory = {},
                    onTreatments = {},
                    onTabSelected = {},
                )
            }
        }
        compose.onNodeWithText("El vínculo fue revocado.").assertIsDisplayed()
        compose.onNodeWithText("92%").assertDoesNotExist()
        compose.onNodeWithText("Losartán 50 mg").assertDoesNotExist()
    }
}
