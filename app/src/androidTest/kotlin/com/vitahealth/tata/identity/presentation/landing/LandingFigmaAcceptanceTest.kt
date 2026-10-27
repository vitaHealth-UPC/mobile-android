package com.vitahealth.tata.identity.presentation.landing

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingFigmaAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val frame = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = requireNotNull(instrumentation.targetContext.getExternalFilesDir(null)).resolve(name)
        file.outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
        frame.recycle()
    }

    private val essential = Plan(
        "ESSENTIAL", "Esencial", BigDecimal("9.90"), "PEN",
        setOf(PlanCapability.REMINDERS, PlanCapability.AGENDA, PlanCapability.INTAKE_CONFIRMATION),
    )
    private val family = Plan(
        "FAMILY", "Familiar", BigDecimal("19.90"), "PEN",
        PlanCapability.entries.toSet(),
    )

    @Test fun landingValueShowsFeaturesAndNavigatesToCatalog() {
        var next = 0
        compose.setContent {
            val ctx = LocalContext.current
            val spanish = remember(ctx) { ctx.withSpanishAppLocale() }
            CompositionLocalProvider(LocalContext provides spanish) {
                TataTheme { LandingValueScreen(onNext = { next++ }) }
            }
        }
        compose.onNodeWithText("Cuidar los medicamentos sin complicarte.").assertIsDisplayed()
        capture("tata-landing1-native-20261026.png")
        compose.onNodeWithText("Funciones principales").performScrollTo().assertIsDisplayed()
        Thread.sleep(900)
        capture("tata-landing1-after-scroll-20261026.png")
        compose.onNodeWithText("Ver planes").performScrollTo().performClick()
        assertEquals(1, next)
    }

    @Test fun catalogShowsServerProvidedPricesAndDoesNotLaunchUnknownContact() {
        val selected = mutableListOf<String>()
        compose.setContent {
            val ctx = LocalContext.current
            val spanish = remember(ctx) { ctx.withSpanishAppLocale() }
            CompositionLocalProvider(LocalContext provides spanish) {
                TataTheme {
                    LandingPlansScreen(
                        LandingPlansUiState(loading = false, plans = listOf(essential, family)),
                        onBack = {}, onRetry = {}, onSelectPlan = { selected += it.code },
                        onContact = null,
                    )
                }
            }
        }
        compose.onNodeWithText("Elige cómo empezar").assertIsDisplayed()
        capture("tata-landing2-native-20261026.png")
        compose.onNodeWithText("S/ 9.90").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("S/ 19.90").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Elegir Familiar").performScrollTo().performClick()
        assertEquals(listOf("FAMILY"), selected)
        compose.onNodeWithText("No disponible").performScrollTo().assertIsNotEnabled()
        capture("tata-landing69-contact-unavailable-20261026.png")
    }

    @Test fun verifiedContactRendersActiveVariantAndDispatchesOneIntentRequest() {
        var contactRequests = 0
        var displayed by mutableStateOf(LandingPlansUiState())
        compose.setContent {
            val ctx = LocalContext.current
            val spanish = remember(ctx) { ctx.withSpanishAppLocale() }
            CompositionLocalProvider(LocalContext provides spanish) {
                TataTheme {
                    LandingPlansScreen(
                        displayed,
                        onBack = {}, onRetry = {}, onSelectPlan = {},
                        onContact = { contactRequests++ },
                    )
                }
            }
        }
        compose.onNodeWithText("Elige cómo empezar").assertIsDisplayed()
        compose.runOnIdle {
            displayed = LandingPlansUiState(loading = false, plans = listOf(essential, family))
        }
        compose.onNodeWithText("Contactar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Qué incluye cada plan").performScrollTo().assertIsDisplayed()
        capture("tata-landing2-active-contact-20261026.png")
        compose.onNodeWithText("Contactar").performClick()
        assertEquals(1, contactRequests)
    }

    @Test fun unavailableCatalogDoesNotDisplayExamplePricesAndCanRetry() {
        var retry = 0
        compose.setContent {
            val ctx = LocalContext.current
            val spanish = remember(ctx) { ctx.withSpanishAppLocale() }
            CompositionLocalProvider(LocalContext provides spanish) {
                TataTheme {
                    LandingPlansScreen(
                        LandingPlansUiState(loading = false, error = true),
                        onBack = {}, onRetry = { retry++ }, onSelectPlan = {},
                    )
                }
            }
        }
        compose.onNodeWithText("S/ 9.90").assertDoesNotExist()
        compose.onNodeWithText("Reintentar").performClick()
        assertEquals(1, retry)
    }
}
