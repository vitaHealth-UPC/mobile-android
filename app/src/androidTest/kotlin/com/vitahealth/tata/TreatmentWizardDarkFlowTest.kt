package com.vitahealth.tata

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentDoseFrequencyScreen
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentScheduleInstructionsScreen
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentReminderScreen
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentLifecycleScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDoseFrequencyUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentScheduleInstructionsUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentReminderUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cross-step treatment wizard UI regression for contrast and working callbacks.
 *
 * Fixtures live only in androidTest. Screens remain wired to the production
 * application handlers; these tests are not remote-backend acceptance evidence.
 */
@RunWith(AndroidJUnit4::class)
class TreatmentWizardDarkFlowTest {
    @get:Rule val compose = createComposeRule()

    private val dose =
        TreatmentDoseFrequencyUiState(
            caregiverId = "caregiver-test",
            olderAdultId = "adult-test",
            olderAdultName = "Rosa Vargas",
            medicationId = "med-test",
            medicationLabel = "Losartán 50 mg",
            treatmentId = "treatment-test",
            treatmentName = "Control de presión",
            dosage = "1 comprimido",
            frequency = "Cada día",
        )

    private val schedule =
        TreatmentScheduleInstructionsUiState(
            caregiverId = dose.caregiverId,
            olderAdultId = dose.olderAdultId,
            olderAdultName = dose.olderAdultName,
            medicationId = dose.medicationId,
            medicationLabel = dose.medicationLabel,
            treatmentId = dose.treatmentId,
            treatmentName = dose.treatmentName,
            dosage = dose.dosage,
            frequency = dose.frequency,
            scheduleText = "08:00, 20:00",
            instructions = "Con agua",
        )

    private val reminder =
        TreatmentReminderUiState(
            caregiverId = schedule.caregiverId,
            olderAdultId = schedule.olderAdultId,
            olderAdultName = schedule.olderAdultName,
            medicationId = schedule.medicationId,
            medicationLabel = schedule.medicationLabel,
            treatmentId = schedule.treatmentId,
            treatmentName = schedule.treatmentName,
            dosage = schedule.dosage,
            frequency = schedule.frequency,
            scheduleText = schedule.scheduleText,
            instructions = schedule.instructions,
            followUpDelayMinutes = "10",
        )

    private val review =
        TreatmentLifecycleUiState(
            caregiverId = reminder.caregiverId,
            olderAdultId = reminder.olderAdultId,
            olderAdultName = reminder.olderAdultName,
            medicationId = reminder.medicationId,
            medicationLabel = reminder.medicationLabel,
            treatmentId = reminder.treatmentId,
            treatmentName = reminder.treatmentName,
            dosage = reminder.dosage,
            frequency = reminder.frequency,
            scheduleText = reminder.scheduleText,
            instructions = reminder.instructions,
            reminderDelayMinutes = 10,
        )

    @Test
    fun doseAndFrequencyDark() {
        render(true) {
            TreatmentDoseFrequencyScreen(dose, {}, {}, {})
        }
        compose.onNodeWithText("1 comprimido").assertIsDisplayed()
        capture("dose-frequency-dark", true)
    }

    @Test
    fun scheduleAndInstructionsDark() {
        render(true) {
            TreatmentScheduleInstructionsScreen(schedule, {}, {}, {})
        }
        compose.onNodeWithText("Horarios (HH:mm)").assertIsDisplayed()
        capture("schedule-dark", true)
    }

    @Test
    fun reminderPolicyDark() {
        render(true) {
            TreatmentReminderScreen(reminder, {}, {})
        }
        compose.onNodeWithText("Recordatorios activos").assertIsDisplayed()
        capture("reminder-dark", true)
    }

    @Test
    fun draftReviewDark() {
        render(true) {
            TreatmentLifecycleScreen(review, {}, {}, {}, {})
        }
        compose.onNodeWithText("Nombre del tratamiento").assertIsDisplayed()
        capture("review-dark", true)
        compose.onNodeWithText("Activar tratamiento").performScrollTo().assertIsDisplayed()
        capture("review-dark-scrolled", true)
    }

    @Test
    fun draftReviewLightPreservesDesign() {
        render(false) {
            TreatmentLifecycleScreen(review, {}, {}, {}, {})
        }
        compose.onNodeWithText("Nombre del tratamiento").assertIsDisplayed()
        capture("review-light", false)
    }

    @Test
    fun doseStepInvokesContinue() {
        var clicked = false
        render(true) {
            TreatmentDoseFrequencyScreen(dose, {}, {}, { clicked = true })
        }
        compose.onNodeWithText("Siguiente").performScrollTo().performClick()
        compose.runOnIdle { assertTrue("Continue callback not invoked", clicked) }
    }

    @Test
    fun reminderStepInvokesContinue() {
        var clicked = false
        render(true) {
            TreatmentReminderScreen(reminder, {}, { clicked = true })
        }
        compose.onNodeWithText("Siguiente").performScrollTo().performClick()
        compose.runOnIdle { assertTrue("Reminder callback not invoked", clicked) }
    }

    @Test
    fun reviewInvokesActivationWithoutBypassingHandler() {
        var clicked = false
        render(true) {
            TreatmentLifecycleScreen(review, { clicked = true }, {}, {}, {})
        }
        compose.onNodeWithText("Activar tratamiento").performScrollTo().performClick()
        compose.runOnIdle { assertTrue("Activation callback not invoked", clicked) }
    }

    private fun render(dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent {
            val original = LocalContext.current
            val spanish = remember(original) { original.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme(TataAccessibility(darkTheme = dark)) {
                    content()
                }
            }
        }
    }

    private fun capture(name: String, dark: Boolean) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap =
            try {
                compose.onRoot().captureToImage().asAndroidBitmap()
            } catch (pixelCopy: AssertionError) {
                requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
                    "Screenshot unavailable: " + name
                }
            }
        check(bitmap.width > 100 && bitmap.height > 100)
        val edge = bitmap.getPixel(4, bitmap.height / 2)
        val lightness = (Color.red(edge) + Color.green(edge) + Color.blue(edge)) / 3
        assertTrue(
            "Unexpected background lightness: " + lightness,
            if (dark) lightness < 110 else lightness > 190,
        )
        val resolver = instrumentation.targetContext.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "tata-wizard-" + name + ".png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
                },
            )
        )
        resolver.openOutputStream(uri)!!.use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
