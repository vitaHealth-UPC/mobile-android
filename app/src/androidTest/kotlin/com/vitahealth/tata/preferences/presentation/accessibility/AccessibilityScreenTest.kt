package com.vitahealth.tata.preferences.presentation.accessibility

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.safeDrawingPadding
import com.vitahealth.tata.shared.design.accessibility.TataAccessibility
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.shared.design.theme.TataTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class AccessibilityScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun settingsRemainInteractiveAndSavingPreventsDuplicateChanges() {
        var clicks = 0
        var saving by mutableStateOf(false)
        compose.setContent {
            var preferences by remember { mutableStateOf(AccessibilityPreferences()) }
            TataTheme(accessibility = TataAccessibility(fontScale = preferences.textSize.scaleFactor,
                highContrast = preferences.highContrast, reducedMotion = preferences.reducedMotion,
                readingAssistance = preferences.readingAssistance)) {
                AccessibilityScreen(AccessibilityUiState(preferences = preferences, isSaving = saving),
                    onBack = {},
                    onLargeTextChange = { clicks++; preferences = preferences.copy(textSize = if (it) TextSizeLevel.LARGE else TextSizeLevel.MEDIUM) },
                    onHighContrastChange = { preferences = preferences.copy(highContrast = it) },
                    onReducedMotionChange = { preferences = preferences.copy(reducedMotion = it) },
                    onVoiceConfirmationChange = { preferences = preferences.copy(voiceConfirmation = it) },
                    onReadingAssistanceChange = { preferences = preferences.copy(readingAssistance = it) },
                    modifier = Modifier.safeDrawingPadding())
            }
        }
        val switches = compose.onAllNodes(isToggleable())
        switches.assertCountEquals(5)
        switches[0].assertIsOff().performClick().assertIsOn()
        switches[1].performClick().assertIsOn()
        switches[2].performClick().assertIsOn()
        switches[3].performClick().assertIsOff()
        switches[4].performClick().assertIsOn()
        compose.runOnIdle { saving = true }
        switches[0].assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = instrumentation.uiAutomation.executeShellCommand("screencap -p /data/local/tmp/accessibility-native.png")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }
}
