package com.vitahealth.tata.carelink.presentation.consent

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ConsentWithdrawalDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cancelDoesNotWithdrawAndConfirmedReceiptLeavesAccessOnce() {
        var writes = 0
        var exits = 0
        val factory =
            ConsentWithdrawalViewModel.Factory(
                "adult",
                ConsentWithdrawal {
                    writes++
                    AppResult.Success(Unit)
                },
            )
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val config =
            android.content.res.Configuration(base.resources.configuration).apply {
                setLocales(android.os.LocaleList.forLanguageTags("es-419"))
            }
        val spanish = base.createConfigurationContext(config)
        compose.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides spanish,
                androidx.compose.ui.platform.LocalConfiguration provides
                    spanish.resources.configuration,
            ) {
                TataTheme { ConsentWithdrawalAction(factory) { exits++ } }
            }
        }
        compose.onNodeWithText("Retirar consentimiento").performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle {
            assertEquals(0, writes)
            assertEquals(0, exits)
        }
        compose.onNodeWithText("Retirar consentimiento").performClick()
        compose.onNodeWithText("¿Retirar consentimiento?").assertIsDisplayed()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        compose.waitForIdle()
        val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        requireNotNull(instrumentation.targetContext.getExternalFilesDir(null))
            .resolve("tata-consent-withdrawal-20261026.png")
            .outputStream()
            .use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
        compose.onNodeWithText("Retirar").performClick()
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(1, writes)
            assertEquals(1, exits)
        }
    }
}
