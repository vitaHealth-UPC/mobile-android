package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertFollowUpActions
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertFollowUpActionsTest {
    @get:Rule val compose = createComposeRule()

    private fun label(id: Int) =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private fun alert() =
        CaregiverAlert(
            1,
            "intake",
            "Medication",
            Instant.EPOCH,
            "Follow up",
            AlertStatus.OPEN,
            Instant.EPOCH,
            null,
        )

    @Test
    fun closeRequiresConfirmationAndCancelDoesNotSendAnUpdate() {
        val updates = mutableListOf<AlertStatus>()
        var current by mutableStateOf(AlertDetailUiState.Content(alert()))
        compose.setContent {
            TataTheme {
                AlertFollowUpActions(
                    current,
                    {
                        updates += it
                        current =
                            current.copy(
                                alert = current.alert.copy(status = it, closedAt = Instant.EPOCH)
                            )
                    },
                )
            }
        }
        compose.onNodeWithText(label(R.string.alert_action_close)).performClick()
        compose.runOnIdle { assertEquals(emptyList<AlertStatus>(), updates) }
        compose.onNodeWithText(label(R.string.alert_close_cancel)).performClick()
        compose.runOnIdle { assertEquals(emptyList<AlertStatus>(), updates) }
        compose.onNodeWithText(label(R.string.alert_action_close)).performClick()
        compose
            .onNode(hasText(label(R.string.alert_action_close)) and hasAnyAncestor(isDialog()))
            .performClick()
        compose.runOnIdle { assertEquals(listOf(AlertStatus.CLOSED), updates) }
        compose.onNodeWithText(label(R.string.alert_action_close)).assertDoesNotExist()
    }

    @Test
    fun mutationBlocksAnotherStateChangeAndOpeningANote() {
        var notes = 0
        var updates = 0
        compose.setContent {
            TataTheme {
                AlertFollowUpActions(
                    AlertDetailUiState.Content(alert(), updating = AlertStatus.ATTENDED),
                    { updates++ },
                    onAddNote = { notes++ },
                )
            }
        }
        compose.onNodeWithText(label(R.string.alert_action_saving)).assertIsNotEnabled()
        compose.onNodeWithText(label(R.string.alert_action_close)).assertIsNotEnabled()
        compose.onNodeWithText(label(R.string.note_add_followup)).assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(0, notes)
            assertEquals(0, updates)
        }
    }
}
