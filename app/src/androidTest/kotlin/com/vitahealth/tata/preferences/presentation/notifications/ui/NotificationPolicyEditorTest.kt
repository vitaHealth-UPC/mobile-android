package com.vitahealth.tata.preferences.presentation.notifications.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises policy draft retention after failure and dismissal only after confirmed success. */
@RunWith(AndroidJUnit4::class)
class NotificationPolicyEditorTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun failedSaveKeepsDraftAndSuccessfulRetryClosesEditor() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var saving by mutableStateOf(false)
        var message by mutableStateOf<NotificationMessage?>(null)
        val submitted = mutableListOf<Int>()
        compose.setContent {
            TataTheme {
                NotificationPolicyCards(
                    policy = NotificationPolicy(5, 10, 20, 30, null, null),
                    contacts = emptyList(),
                    enabled = !saving,
                    saveMessage = message,
                    onSave = { policy, _ ->
                        submitted += policy.reminderMinutes
                        message = null
                        saving = true
                    },
                )
            }
        }
        compose
            .onNodeWithText(context.getString(R.string.notifications_policy_reminders))
            .performClick()
        compose.onNodeWithText("5", useUnmergedTree = true).performTextReplacement("7")
        compose.onNodeWithText(context.getString(R.string.notifications_policy_save)).performClick()
        compose.runOnIdle {
            saving = false
            message = NotificationMessage.ErrorOffline
        }
        compose.onNodeWithText("7", useUnmergedTree = true).assertExists()
        compose.onNodeWithText(context.getString(R.string.notifications_policy_save)).performClick()
        compose.runOnIdle {
            saving = false
            message = NotificationMessage.Saved
        }
        compose
            .onNodeWithText(context.getString(R.string.notifications_policy_save))
            .assertDoesNotExist()
        assertEquals(listOf(7, 7), submitted)
    }
}
