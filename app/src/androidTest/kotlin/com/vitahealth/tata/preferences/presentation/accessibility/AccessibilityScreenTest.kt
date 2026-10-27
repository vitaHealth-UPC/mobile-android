package com.vitahealth.tata.preferences.presentation.accessibility

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.presentation.accessibility.screens.AccessibilityScreen
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun settingsRemainInteractiveAndSavingPreventsDuplicateChanges() {
        var clicks = 0
        var saving by mutableStateOf(false)
        compose.setContent {
            var preferences by remember { mutableStateOf(AccessibilityPreferences()) }
            TataTheme(
                accessibility =
                    TataAccessibility(
                        fontScale = preferences.textSize.scaleFactor,
                        highContrast = preferences.highContrast,
                        darkTheme = preferences.darkTheme,
                        reducedMotion = preferences.reducedMotion,
                        readingAssistance = preferences.readingAssistance,
                    )
            ) {
                AccessibilityScreen(
                    AccessibilityUiState(preferences = preferences, isSaving = saving),
                    onBack = {},
                    onLargeTextChange = {
                        clicks++
                        preferences =
                            preferences.copy(
                                textSize = if (it) TextSizeLevel.LARGE else TextSizeLevel.MEDIUM
                            )
                    },
                    onDarkThemeChange = { preferences = preferences.copy(darkTheme = it) },
                    onHighContrastChange = { preferences = preferences.copy(highContrast = it) },
                    onReducedMotionChange = { preferences = preferences.copy(reducedMotion = it) },
                    onVoiceConfirmationChange = {
                        preferences = preferences.copy(voiceConfirmation = it)
                    },
                    onReadingAssistanceChange = {
                        preferences = preferences.copy(readingAssistance = it)
                    },
                    modifier = Modifier.safeDrawingPadding(),
                )
            }
        }
        val switches = compose.onAllNodes(isToggleable())
        switches.assertCountEquals(6)
        switches[0].assertIsOff().performClick().assertIsOn()
        switches[1].performScrollTo().performClick().assertIsOn()
        switches[2].performScrollTo().performClick().assertIsOn()
        switches[3].performScrollTo().performClick().assertIsOn()
        switches[4].performScrollTo().performClick().assertIsOff()
        switches[5].performScrollTo().performClick().assertIsOn()
        compose.runOnIdle { saving = true }
        switches[0].performScrollTo().assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output =
            instrumentation.uiAutomation.executeShellCommand(
                "screencap -p /data/local/tmp/accessibility-native.png"
            )
        android.os.ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }

    @Test
    fun bottomNavigationRemainsVisibleAfterScrollingSettings() {
        var selected: AdultTab? = null
        compose.setContent {
            TataTheme {
                AccessibilityScreen(
                    AccessibilityUiState(),
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    modifier = Modifier.safeDrawingPadding(),
                    navigationBar = {
                        AdultTabBar(AdultTab.More, { selected = it }, AdultTab.entries.toSet())
                    },
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose
            .onNodeWithText(context.getString(R.string.accessibility_help_title))
            .performScrollTo()
        compose
            .onNodeWithText(context.getString(AdultTab.Agenda.label))
            .assertIsDisplayed()
            .performClick()
        compose.runOnIdle { assertEquals(AdultTab.Agenda, selected) }
    }
}
