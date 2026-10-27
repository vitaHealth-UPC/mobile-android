package com.vitahealth.tata.preferences.language

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withTataAppLocale
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.preferences.presentation.accessibility.screens.AccessibilityScreen
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageFigmaVisualTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun screen(tag: String, language: AppLanguage, onChange: () -> Unit) {
        compose.setContent {
            val original = LocalContext.current
            val configured = remember(original, tag) { original.withTataAppLocale(tag) }
            CompositionLocalProvider(LocalContext provides configured) {
                TataTheme {
                    AccessibilityScreen(
                        state = AccessibilityUiState(appLanguage = language),
                        onBack = {},
                        onLargeTextChange = {},
                        onHighContrastChange = {},
                        onReducedMotionChange = {},
                        onVoiceConfirmationChange = {},
                        onReadingAssistanceChange = {},
                        onLanguageChange = onChange,
                    )
                }
            }
        }
    }

    private fun capture(filename: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val destination = requireNotNull(
            instrumentation.targetContext.getExternalFilesDir(null)
        ).resolve(filename)
        val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        destination.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }

    @Test fun figma70SpanishSelectorIsClickable() {
        var clicks = 0
        screen("es-419", AppLanguage.SPANISH) { clicks++ }
        compose.onNodeWithText("Idioma").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("‹ Español ›").performScrollTo().assertIsDisplayed()
        capture("tata-figma70-es-20261026.png")
        compose.onNodeWithText("‹ Español ›").performClick()
        assertEquals(1, clicks)
    }

    @Test fun englishResourceAndSelectedLanguageAreVisible() {
        var clicks = 0
        screen("en", AppLanguage.ENGLISH) { clicks++ }
        compose.onNodeWithText("Language").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("App language").assertIsDisplayed()
        compose.onNodeWithText("‹ English ›").performScrollTo().assertIsDisplayed()
        capture("tata-figma70-en-20261026.png")
        compose.onNodeWithText("‹ English ›").performClick()
        assertEquals(1, clicks)
    }
}
