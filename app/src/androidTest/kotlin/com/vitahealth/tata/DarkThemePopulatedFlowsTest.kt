package com.vitahealth.tata

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.presentation.medication.screens.MedicationRegistrationScreen
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationRegistrationUiState
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentCreationScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentCreationUiState
import java.time.Instant
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Native dark-theme coverage for populated, actionable flows beyond empty states.
 *
 * Data stays in androidTest. Production remains backed by authorized repositories.
 * A real render is required: screenshot failures are not silently ignored.
 */
@RunWith(AndroidJUnit4::class)
class DarkThemePopulatedFlowsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun medicationWizardRetainsInputsInDarkTheme() {
        render {
            MedicationRegistrationScreen(
                state =
                    MedicationRegistrationUiState(
                        caregiverId = "caregiver-test",
                        olderAdultId = "adult-test",
                        olderAdultName = "Rosa Vargas",
                        name = "Losartán",
                        presentation = "Tableta",
                        frequency = "Diaria",
                    ),
                onNameChange = {},
                onPresentationChange = {},
                onFrequencyChange = {},
                onTimingChange = {},
                onNotesChange = {},
                onSubmit = {},
            )
        }
        compose.onNodeWithText("Agregar medicamento", substring = true).assertExists()
        capture("medication-form-dark")
    }

    @Test
    fun treatmentCreationPreservesActualFormFieldsInDarkTheme() {
        render {
            TreatmentCreationScreen(
                state =
                    TreatmentCreationUiState(
                        caregiverId = "caregiver-test",
                        olderAdultId = "adult-test",
                        olderAdultName = "Rosa Vargas",
                        medicationId = "medication-test",
                        medicationLabel = "Losartán 50 mg",
                        name = "Control de presión",
                    ),
                onNameChange = {},
                onCreate = {},
            )
        }
        compose.onAllNodesWithText("Crear tratamiento").onFirst().assertIsDisplayed()
        compose.onNodeWithText("Losartán 50 mg").assertIsDisplayed()
        capture("treatment-create-dark")
    }

    @Test
    fun notesWithLinkedProfileKeepReadableCardsInDarkTheme() {
        render {
            NotesScreen(
                state =
                    NotesUiState.Content(
                        listOf(
                            FollowUpNote(
                                27,
                                "Comprobó el medicamento por la mañana.",
                                Instant.parse("2026-10-24T15:00:00Z"),
                                "caregiver-test",
                            )
                        )
                    ),
                caregiverId = "caregiver-test",
                person =
                    MonitoredPerson(
                        "adult-test",
                        "Rosa Vargas",
                        LocalDate.of(1958, 5, 12),
                    ),
            )
        }
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
        compose.onNodeWithText("Comprobó el medicamento por la mañana.").assertIsDisplayed()
        capture("notes-with-profile-dark")
    }

    private fun render(screen: @Composable () -> Unit) {
        compose.setContent {
            val original = LocalContext.current
            val spanish = remember(original) { original.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme(TataAccessibility(darkTheme = true)) { screen() }
            }
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // API 36 AVD occasionally drops PixelCopy. A system screenshot is a
        // valid fallback, but never count a null/empty image as a visual pass.
        val bitmap =
            try {
                compose.onRoot().captureToImage().asAndroidBitmap()
            } catch (pixelCopy: AssertionError) {
                requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
                    "Both PixelCopy and UIAutomation screenshots failed for $name"
                }
            }
        check(bitmap.width > 0 && bitmap.height > 0)
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "tata-extended-$name.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
                },
            )
        )
        resolver.openOutputStream(uri)!!.use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}
