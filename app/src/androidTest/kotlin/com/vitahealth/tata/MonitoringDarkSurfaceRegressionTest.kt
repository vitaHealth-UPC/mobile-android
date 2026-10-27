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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.presentation.personalnotes.screens.PersonalNotesScreen
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesUiState
import com.vitahealth.tata.monitoring.presentation.summary.screens.FamilySummaryScreen
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Light-theme regression and dark-theme coverage for populated Monitoring surfaces.
 *
 * Uses real production Compose screens with fixture read models. Backend integration
 * and authorization are tested separately; screenshots are not counted as Figma passes.
 */
@RunWith(AndroidJUnit4::class)
class MonitoringDarkSurfaceRegressionTest {
    @get:Rule val compose = createComposeRule()

    private val summary =
        FamilySummary(
            olderAdultName = "Rosa Vargas",
            date = LocalDate.of(2026, 10, 25),
            weekly = AdherenceCounts(12, 14),
            today = AdherenceCounts(2, 3),
            nextDose = null,
            stockAttention = emptyList(),
            openAlerts = emptyList(),
            inventoryAvailable = true,
        )

    private val notes =
        listOf(
            PersonalNote(
                id = 10L,
                title = "Registro de rutina",
                text = "Me sentí bien después del desayuno.",
                category = PersonalNoteCategory.ROUTINE,
                recordedAt = Instant.now(),
            ),
            PersonalNote(
                id = 11L,
                title = "Nota anterior",
                text = "Registro previo de toma.",
                category = PersonalNoteCategory.MEDICATION,
                recordedAt = Instant.now().minusSeconds(86400),
            ),
        )

    @Test
    fun populatedFamilySummaryIsLegibleInDarkTheme() {
        render(dark = true) {
            FamilySummaryScreen(
                state = FamilySummaryUiState(loading = false, summary = summary),
                olderAdultName = "Rosa Vargas",
                onRetry = {},
                onAgenda = {},
                onHistory = {},
                onContact = {},
                onAlerts = {},
                onNotes = {},
                onAddMedication = {},
                onChangePerson = {},
            )
        }
        compose.onNodeWithText("Resumen familiar").assertIsDisplayed()
        compose.onNodeWithText("Adherencia esta semana").assertIsDisplayed()
        capture("family-summary-dark", dark = true)
    }

    @Test
    fun populatedFamilySummaryPreservesLightTheme() {
        render(dark = false) {
            FamilySummaryScreen(
                state = FamilySummaryUiState(loading = false, summary = summary),
                olderAdultName = "Rosa Vargas",
                onRetry = {},
                onAgenda = {},
                onHistory = {},
                onContact = {},
                onAlerts = {},
                onNotes = {},
                onAddMedication = {},
                onChangePerson = {},
            )
        }
        compose.onNodeWithText("Resumen familiar").assertIsDisplayed()
        capture("family-summary-light", dark = false)
    }

    @Test
    fun familySummaryDenialDoesNotRenderPatientStatistics() {
        render(dark = true) {
            FamilySummaryScreen(
                state = FamilySummaryUiState(
                    loading = false,
                    errorCode = "CARE_RELATIONSHIP_REQUIRED",
                ),
                olderAdultName = "Rosa Vargas",
                onRetry = {},
                onAgenda = {},
                onHistory = {},
                onContact = {},
                onAlerts = {},
                onNotes = {},
                onAddMedication = {},
                onChangePerson = {},
            )
        }
        compose.onNodeWithText("Seguimiento restringido").assertIsDisplayed()
        compose.onNodeWithText("Adherencia esta semana").assertDoesNotExist()
        capture("family-consent-dark", dark = true)
    }

    @Test
    fun personalNotesCardsAreReadableInDarkTheme() {
        render(dark = true) {
            PersonalNotesScreen(
                state = PersonalNotesUiState(notes = notes, loading = false),
                onRetry = {},
                onFilter = {},
                onAdd = {},
                onTab = {},
                onSignIn = {},
            )
        }
        compose.onNodeWithText("Registro de rutina").assertIsDisplayed()
        capture("personal-notes-dark", dark = true)
    }

    @Test
    fun personalNotesRetainLightFigmaPalette() {
        render(dark = false) {
            PersonalNotesScreen(
                state = PersonalNotesUiState(notes = notes, loading = false),
                onRetry = {},
                onFilter = {},
                onAdd = {},
                onTab = {},
                onSignIn = {},
            )
        }
        compose.onNodeWithText("Registro de rutina").assertIsDisplayed()
        capture("personal-notes-light", dark = false)
    }

    @Test
    fun familySummaryQuickActionStillNavigatesInDarkTheme() {
        var agendaRequested = false
        render(dark = true) {
            FamilySummaryScreen(
                state = FamilySummaryUiState(loading = false, summary = summary),
                olderAdultName = "Rosa Vargas",
                onRetry = {},
                onAgenda = { agendaRequested = true },
                onHistory = {},
                onContact = {},
                onAlerts = {},
                onNotes = {},
                onAddMedication = {},
                onChangePerson = {},
            )
        }
        compose.onNodeWithText("Ver agenda").performClick()
        compose.runOnIdle { assertTrue("Agenda callback was not invoked", agendaRequested) }
    }

    @Test
    fun notesAddActionRemainsClickableInDarkTheme() {
        var addRequested = false
        render(dark = true) {
            PersonalNotesScreen(
                state = PersonalNotesUiState(notes = notes, loading = false),
                onRetry = {},
                onFilter = {},
                onAdd = { addRequested = true },
                onTab = {},
                onSignIn = {},
            )
        }
        compose.onNodeWithText("Agregar nota").performClick()
        compose.runOnIdle { assertTrue("Add note callback was not invoked", addRequested) }
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
            } catch (failure: AssertionError) {
                requireNotNull(instrumentation.uiAutomation.takeScreenshot()) {
                    "Both Compose and system screenshots failed: " + name
                }
            }
        check(bitmap.width > 100 && bitmap.height > 100)
        val backgroundPixel = bitmap.getPixel(4, bitmap.height / 2)
        val luminance = (
            Color.red(backgroundPixel) +
                Color.green(backgroundPixel) +
                Color.blue(backgroundPixel)
        ) / 3
        assertTrue(
            "Unexpected background luminance: " + luminance,
            if (dark) luminance < 110 else luminance > 190,
        )

        val resolver = instrumentation.targetContext.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "tata-monitoring-" + name + ".png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
                },
            )
        )
        resolver.openOutputStream(uri)!!.use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
    }
}
