package com.vitahealth.tata.preferences.presentation.accessibility

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.preferences.presentation.accessibility.screens.AccessibilityScreen
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InternationalizationVisualTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Composable
    private fun SpanishScreen(content: @Composable () -> Unit) {
        val context = LocalContext.current
        val spanish = remember(context) {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(java.util.Locale.forLanguageTag("es-419"))
            context.createConfigurationContext(config)
        }
        CompositionLocalProvider(LocalContext provides spanish) {
            TataTheme { content() }
        }
    }

    @Test fun languageCardRendersAndChangesSelection() {
        var language by mutableStateOf(AppLanguage.SPANISH)
        var changes = 0
        compose.setContent {
            SpanishScreen {
                AccessibilityScreen(
                    state = AccessibilityUiState(appLanguage = language),
                    onBack = {}, onLargeTextChange = {},
                    onHighContrastChange = {}, onReducedMotionChange = {},
                    onVoiceConfirmationChange = {}, onReadingAssistanceChange = {},
                    onLanguageChange = { language = language.next(); changes++ },
                )
            }
        }
        compose.onNodeWithText("‹ Español ›").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("‹ Español ›").performClick()
        compose.onNodeWithText("‹ English ›").assertIsDisplayed()
        assertEquals(1, changes)
        val instrument = InstrumentationRegistry.getInstrumentation()
        val file = requireNotNull(instrument.targetContext.getExternalFilesDir(null))
            .resolve("tata-i18n70-native-20261026.png")
        val bitmap = requireNotNull(instrument.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
