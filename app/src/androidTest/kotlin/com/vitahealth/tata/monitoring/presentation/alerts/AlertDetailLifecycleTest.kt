package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.commands.RegisterFollowUpNoteCommandHandler
import com.vitahealth.tata.monitoring.application.commands.UpdateAlertStatusCommandHandler
import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.repositories.AlertsRepository
import com.vitahealth.tata.monitoring.domain.repositories.NotesRepository
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertDetailLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resumeRechecksAccessWithoutDuplicatingInitialLoad() {
        val reads = AtomicInteger()
        val revoked = AtomicBoolean(false)
        val now = Instant.parse("2026-10-24T13:00:00Z")
        val alert =
            CaregiverAlert(
                1,
                "intake",
                "Lifecycle medication",
                now,
                "Follow up",
                AlertStatus.OPEN,
                now,
                null,
            )
        val denied = AppResult.Failure("Access revoked", code = AlertFailureCodes.ACCESS_DENIED)
        val alerts =
            object : AlertsRepository {
                override suspend fun openAlerts(caregiverId: String, olderAdultId: String) =
                    AppResult.Success(emptyList<CaregiverAlert>())

                override suspend fun alert(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                ): AppResult<CaregiverAlert> {
                    reads.incrementAndGet()
                    return if (revoked.get()) denied else AppResult.Success(alert)
                }

                override suspend fun updateStatus(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                    status: AlertStatus,
                ): AppResult<CaregiverAlert> = error("No mutation expected")
            }
        val notes =
            object : NotesRepository {
                override suspend fun notes(caregiverId: String, olderAdultId: String) =
                    AppResult.Success(emptyList<FollowUpNote>())

                override suspend fun register(
                    caregiverId: String,
                    olderAdultId: String,
                    text: String,
                ): AppResult<FollowUpNote> = error("No save expected")
            }
        val factory =
            AlertDetailViewModel.Factory(
                "caregiver",
                "adult",
                1,
                GetAlertDetailQueryHandler(alerts),
                UpdateAlertStatusCommandHandler(alerts),
                RegisterFollowUpNoteCommandHandler(notes),
                ContactOptionReader { AppResult.Success(ContactOption(null, "Rosa")) },
            )
        compose.setContent { TataTheme { AlertDetailRoute(factory, {}, {}) } }
        compose.waitUntil(5000) { reads.get() == 1 }
        compose.waitForIdle()
        compose.onNodeWithText("Lifecycle medication").assertExists()
        assertEquals(1, reads.get())
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        revoked.set(true)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.waitUntil(5000) { reads.get() == 2 }
        compose.waitForIdle()
        compose.onNodeWithText("Lifecycle medication").assertDoesNotExist()
        assertEquals(2, reads.get())
    }

    @Test
    fun savesANoteThroughTheRealRouteAndRejectsBlankAndDuplicateSubmission() {
        val now = Instant.parse("2026-10-25T13:00:00Z")
        val alert =
            CaregiverAlert(1, "intake", "Medication", now, "Follow up", AlertStatus.OPEN, now, null)
        val saves = AtomicInteger()
        val pending = CompletableDeferred<Unit>()
        var submitted = ""
        val alerts =
            object : AlertsRepository {
                override suspend fun openAlerts(caregiverId: String, olderAdultId: String) =
                    AppResult.Success(emptyList<CaregiverAlert>())

                override suspend fun alert(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                ) = AppResult.Success(alert)

                override suspend fun updateStatus(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                    status: AlertStatus,
                ): AppResult<CaregiverAlert> = error("No state change expected")
            }
        val notes =
            object : NotesRepository {
                override suspend fun notes(caregiverId: String, olderAdultId: String) =
                    AppResult.Success(emptyList<FollowUpNote>())

                override suspend fun register(
                    caregiverId: String,
                    olderAdultId: String,
                    text: String,
                ): AppResult<FollowUpNote> {
                    assertEquals("caregiver", caregiverId)
                    assertEquals("adult", olderAdultId)
                    saves.incrementAndGet()
                    submitted = text
                    pending.await()
                    return AppResult.Success(FollowUpNote(1, text, now, caregiverId))
                }
            }
        val factory =
            AlertDetailViewModel.Factory(
                "caregiver",
                "adult",
                1,
                GetAlertDetailQueryHandler(alerts),
                UpdateAlertStatusCommandHandler(alerts),
                RegisterFollowUpNoteCommandHandler(notes),
                ContactOptionReader { AppResult.Success(ContactOption(null, "Rosa")) },
            )
        compose.setContent { TataTheme { AlertDetailRoute(factory, {}, {}) } }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose
            .onNodeWithText(context.getString(R.string.note_add_followup))
            .performScrollTo()
            .performClick()
        compose.onNodeWithText(context.getString(R.string.note_dialog_save)).performClick()
        compose.onNodeWithText(context.getString(R.string.note_error_blank)).assertIsDisplayed()
        assertEquals(0, saves.get())
        compose.onNode(hasSetTextAction()).performTextReplacement("  Seguimiento realizado  ")
        compose.onNodeWithText(context.getString(R.string.note_dialog_save)).performClick()
        compose.waitUntil(5000) { saves.get() == 1 }
        compose.onNodeWithText(context.getString(R.string.alert_action_saving)).assertIsNotEnabled()
        compose.onNodeWithText(context.getString(R.string.note_dialog_cancel)).assertIsNotEnabled()
        compose.runOnIdle { pending.complete(Unit) }
        compose.waitForIdle()
        compose.onNodeWithText(context.getString(R.string.note_dialog_title)).assertDoesNotExist()
        compose
            .onNodeWithText(context.getString(R.string.note_saved), substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, saves.get())
            assertEquals("Seguimiento realizado", submitted)
        }
    }
}
