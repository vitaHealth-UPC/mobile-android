package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.ContactUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertWithoutContactTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun missingChannelDoesNotOfferContactAndStillAllowsFollowUp() {
        val now = Instant.parse("2026-10-25T13:00:00Z")
        var alert by
            mutableStateOf(
                CaregiverAlert(
                    1,
                    "intake",
                    "Medication",
                    now,
                    "Follow up",
                    AlertStatus.OPEN,
                    now,
                    null,
                )
            )
        var notes = 0
        compose.setContent {
            TataTheme {
                Box(Modifier.safeDrawingPadding()) {
                    AlertDetailScreen(
                        state = AlertDetailUiState.Content(alert),
                        contact = ContactUiState.Ready(ContactOption(null, "Rosa")),
                        onUpdateStatus = { alert = alert.copy(status = it) },
                        onAddNote = { notes++ },
                    )
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose
            .onNodeWithText(context.getString(R.string.contact_unavailable))
            .performScrollTo()
            .assertIsDisplayed()
            .assertHasNoClickAction()
        compose
            .onNodeWithText(context.getString(R.string.contact_action_named, "Rosa"))
            .assertDoesNotExist()
        compose
            .onNodeWithText(context.getString(R.string.alert_action_attend))
            .performScrollTo()
            .performClick()
        compose.runOnIdle { assertEquals(AlertStatus.ATTENDED, alert.status) }
        compose
            .onNodeWithText(context.getString(R.string.note_add_followup))
            .performScrollTo()
            .performClick()
        compose.runOnIdle { assertEquals(1, notes) }
    }
}
