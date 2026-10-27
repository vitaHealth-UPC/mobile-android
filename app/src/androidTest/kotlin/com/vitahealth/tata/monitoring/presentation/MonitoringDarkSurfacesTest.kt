package com.vitahealth.tata.monitoring.presentation

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
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.monitoring.application.readmodels.StockAttention
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertsScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
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
 * Cross-theme regression gate for Monitoring's actual Compose screens.
 *
 * Synthetic data is confined to AndroidTest; it never substitutes the production
 * HTTP readers or represents an authenticated backend session. Both themes must
 * remain readable without replacing Figma's light assets or changing navigation.
 */
@RunWith(AndroidJUnit4::class)
class MonitoringDarkSurfacesTest {
    @get:Rule val compose = createComposeRule()

    private val summary =
        FamilySummary(
            olderAdultName = "Rosa Vargas",
            date = LocalDate.of(2026, 10, 25),
            weekly = AdherenceCounts(12, 14),
            today = AdherenceCounts(2, 3),
            nextDose =
                MonitoredDose(
                    "dose-test",
                    "med-test",
                    "Losartán 50 mg",
                    "1 tableta",
                    Instant.parse("2026-10-25T19:00:00Z"),
                    "PENDING",
                ),
            stockAttention = listOf(StockAttention("med-test", "Losartán 50 mg", 5)),
            openAlerts = emptyList(),
            inventoryAvailable = true,
        )

    private val notes =
        PersonalNotesUiState(
            loading = false,
            notes =
                listOf(
                    PersonalNote(
                        id = 100,
                        title = "Registrar agua",
                        text = "Anotar la cantidad de agua de hoy.",
                        category = PersonalNoteCategory.ROUTINE,
                        recordedAt = Instant.parse("2026-10-24T16:00:00Z"),
                    ),
                ),
        )

    @Test
    fun summaryDarkMaintainsDashboardAndLowerActions() {
        render(dark = true) {
            FamilySummaryScreen(
                FamilySummaryUiState(loading = false, summary = summary),
                "Rosa Vargas",
                {}, {}, {}, {}, {}, {}, {}, {},
            )
        }
        compose.onNodeWithText("Resumen familiar").assertIsDisplayed()
        capture("family-summary-dark", dark = true)
        compose.onNodeWithText("Monitoreo rápido").performScrollTo().assertIsDisplayed()
        capture("family-summary-dark-actions", dark = true)
    }

    @Test
    fun summaryLightRetainsExistingFigmaPalette() {
        render(dark = false) {
            FamilySummaryScreen(
                FamilySummaryUiState(loading = false, summary = summary),
                "Rosa Vargas",
                {}, {}, {}, {}, {}, {}, {}, {},
            )
        }
        compose.onNodeWithText("Resumen familiar").assertIsDisplayed()
        capture("family-summary-light", dark = false)
    }

    @Test
    fun personalNotesDarkKeepsFiltersAndRealNoteCardsReadable() {
        render(dark = true) {
            PersonalNotesScreen(
                notes,
                onRetry = {},
                onFilter = {},
                onAdd = {},
                onTab = {},
                onSignIn = {},
            )
        }
        compose.onNodeWithText("Registrar agua").assertIsDisplayed()
        capture("personal-notes-dark", dark = true)
    }

    @Test
    fun personalNotesLightKeepsOriginalPastelPalette() {
        render(dark = false) {
            PersonalNotesScreen(
                notes,
                onRetry = {},
                onFilter = {},
                onAdd = {},
                onTab = {},
                onSignIn = {},
            )
        }
        compose.onNodeWithText("Registrar agua").assertIsDisplayed()
        capture("personal-notes-light", dark = false)
    }

    @Test
    fun alertListDarkKeepsActualStatusesAndCardsReadable() {
        render(dark = true) {
            AlertsScreen(
                state =
                    AlertsUiState.Content(
                        alerts = listOf(visualAlert()),
                        olderAdultName = "Rosa Vargas",
                        day = LocalDate.of(2026, 10, 25),
                    ),
            )
        }
        compose.onNodeWithText("Losartán 50 mg").assertIsDisplayed()
        capture("alerts-list-dark", dark = true)
    }

    @Test
    fun alertListLightKeepsOriginalCardsAndIllustrations() {
        render(dark = false) {
            AlertsScreen(
                state =
                    AlertsUiState.Content(
                        alerts = listOf(visualAlert()),
                        olderAdultName = "Rosa Vargas",
                        day = LocalDate.of(2026, 10, 25),
                    ),
            )
        }
        compose.onNodeWithText("Losartán 50 mg").assertIsDisplayed()
        capture("alerts-list-light", dark = false)
    }

    @Test
    fun alertDetailDarkKeepsRealFollowUpActionsLegible() {
        render(dark = true) {
            AlertDetailScreen(
                state = AlertDetailUiState.Content(visualAlert()),
            )
        }
        compose.onNodeWithText("Losartán 50 mg").assertIsDisplayed()
        capture("alert-detail-dark", dark = true)
    }

    private fun visualAlert() =
        CaregiverAlert(
            id = 42,
            intakeId = "test-intake",
            medicationName = "Losartán 50 mg",
            scheduledAt = Instant.parse("2026-10-25T13:00:00Z"),
            reason = "Toma pendiente",
            status = AlertStatus.OPEN,
            openedAt = Instant.parse("2026-10-25T13:10:00Z"),
            closedAt = null,
        )

    private fun render(dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent {
            val original = LocalContext.current
            val spanish = remember(original) { original.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme(accessibility = TataAccessibility(darkTheme = dark)) {
                    content()
                }
            }
        }
    }

    /**
     * Capturing failures must fail the visual gate. Merely compiling is not a
     * successful screenshot or Figma verification.
     */
    private fun capture(name: String, dark: Boolean) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val edge = bitmap.getPixel(4, bitmap.height / 2)
        val luminance = (Color.red(edge) + Color.green(edge) + Color.blue(edge)) / 3
        assertTrue(
            "Unexpected $name background luminance: $luminance",
            if (dark) luminance < 105 else luminance > 210,
        )

        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri =
            requireNotNull(
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "tata-monitoring-$name.png")
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
