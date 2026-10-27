package com.vitahealth.tata.monitoring.presentation.alerts

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.vitahealth.tata.R
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Isolated visual/semantics fixtures for Figma states 18, 56, 57 and 58.
 *
 * Recorded reminders are explicit fixtures, never inferred from their scheduled timestamps.
 * End-to-end server authorization is tested separately.
 */
@RunWith(AndroidJUnit4::class)
class MonitoringFigmaStatesTest {
    @get:Rule val compose = createComposeRule()

    private val scheduledAt = Instant.parse("2026-10-25T13:00:00Z")

    private fun alert(status: AlertStatus = AlertStatus.OPEN) =
        CaregiverAlert(
            42L,
            "intake-test",
            "Losartán 50 mg",
            scheduledAt,
            "Toma pendiente",
            status,
            scheduledAt.plusSeconds(600),
            null,
        )

    private val person = MonitoredPerson("adult-test", "Rosa Vargas", LocalDate.of(1958, 5, 12))

    private val recordedEvents =
        listOf(
            RecordedAlertEvent(RecordedAlertEvent.Type.INTAKE_SCHEDULED, scheduledAt),
            RecordedAlertEvent(
                RecordedAlertEvent.Type.REINFORCED_REMINDER_SENT,
                scheduledAt.plusSeconds(300),
            ),
            RecordedAlertEvent(
                RecordedAlertEvent.Type.ALERT_GENERATED,
                scheduledAt.plusSeconds(600),
            ),
        )

    private fun display(
        status: AlertStatus = AlertStatus.OPEN,
        contact: ContactUiState,
        noteSaved: Boolean = false,
        captureName: String = "alert-detail-initial",
    ) {
        compose.setContent {
            ProductionSpanishTheme {
                Box(Modifier.safeDrawingPadding()) {
                    AlertDetailScreen(
                        state =
                            AlertDetailUiState.Content(
                                alert(status),
                                dose =
                                    MonitoredDose(
                                        "intake-test",
                                        "med-test",
                                        "Losartán 50 mg",
                                        "1 tableta",
                                        scheduledAt,
                                        "PENDING",
                                    ),
                            ),
                        contact = contact,
                        person = person,
                        history = recordedEvents,
                        noteSaved = noteSaved,
                    )
                }
            }
        }
        capture(captureName)
    }

    private fun label(id: Int, vararg args: Any) =
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .withSpanishAppLocale()
            .getString(id, *args)

    @Test
    fun detailUsesOnlyRecordedReminderEvidence() {
        display(
            contact =
                ContactUiState.Ready(
                    ContactOption(ContactChannel(ContactChannelType.PHONE, "+51999999888"), "Rosa")
                )
        )
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
        compose
            .onNodeWithText(label(R.string.alert_timeline_reinforced_sent))
            .performScrollTo()
            .assertIsDisplayed()
        compose
            .onNodeWithText(label(R.string.contact_action_named, "Rosa"))
            .performScrollTo()
            .assertIsDisplayed()
        capture("alert-detail-complete")
    }

    @Test
    fun contactUnavailableRemainsReadableAndFollowUpIsStillPossible() {
        display(
            contact = ContactUiState.Ready(ContactOption(null, "Rosa")),
            captureName = "contact-unavailable-initial",
        )
        compose
            .onNodeWithText(label(R.string.contact_unavailable))
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText(label(R.string.contact_action_named, "Rosa")).assertDoesNotExist()
        compose
            .onNodeWithText(label(R.string.note_add_followup))
            .performScrollTo()
            .assertIsDisplayed()
        capture("contact-unavailable")
    }

    @Test
    fun savedFollowUpShowsConfirmationWithoutFabricatingAlertHistory() {
        display(
            contact =
                ContactUiState.Ready(
                    ContactOption(ContactChannel(ContactChannelType.PHONE, "+51999999888"), "Rosa")
                ),
            noteSaved = true,
            captureName = "followup-note-saved-initial",
        )
        compose
            .onNodeWithText(label(R.string.note_saved), substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        capture("followup-note-saved")
    }

    @Test
    fun attendedAlertShowsNonActionableCompletedState() {
        display(
            status = AlertStatus.ATTENDED,
            captureName = "alert-attended-initial",
            contact =
                ContactUiState.Ready(
                    ContactOption(ContactChannel(ContactChannelType.PHONE, "+51999999888"), "Rosa")
                ),
        )
        capture("alert-attended")
        compose.onNodeWithText(label(R.string.alert_action_attend)).assertDoesNotExist()
        compose
            .onNodeWithText(label(R.string.note_add_followup))
            .performScrollTo()
            .assertIsDisplayed()
    }

    /** A missing capture fails this visual gate, instead of silently approving absent evidence. */
    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "monitoring-$name.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
            }
        val image =
            requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        resolver.openOutputStream(image)!!.use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
    }
}

/** Mirrors MainActivity es-419, including formatters reading LocalConfiguration. */
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
