package com.vitahealth.tata.identity.presentation.subscription

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
import com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
import com.vitahealth.tata.identity.presentation.subscription.screens.PlanSubscriptionScreen
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanChangeMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Composable
private fun SpanishTata(content: @Composable () -> Unit) {
    val base = LocalContext.current
    val spanish = remember(base) {
        val config = android.content.res.Configuration(base.resources.configuration)
        config.setLocale(java.util.Locale.forLanguageTag("es-PE"))
        base.createConfigurationContext(config)
    }
    CompositionLocalProvider(LocalContext provides spanish) {
        TataTheme { content() }
    }
}

@RunWith(AndroidJUnit4::class)
class PlanSubscriptionVisualAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val essential = Plan(
        "ESSENTIAL", "Esencial", BigDecimal("9.90"), "PEN",
        setOf(PlanCapability.REMINDERS, PlanCapability.AGENDA, PlanCapability.INTAKE_CONFIRMATION)
    )
    private val family = Plan(
        "FAMILY", "Familiar", BigDecimal("19.90"), "PEN",
        PlanCapability.entries.toSet()
    )
    private val subscription = Subscription(
        "caregiver", family, SubscriptionStatus.ACTIVE,
        Instant.parse("2026-10-28T00:00:00Z")
    )

    private fun capture(filename: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = requireNotNull(instrumentation.targetContext.getExternalFilesDir(null))
            .resolve(filename)
        instrumentation.waitForIdleSync()
        Thread.sleep(750) // Wait for the Compose surface to finish drawing on the emulator.
        val image = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }

    @Test fun basePlanScreenShowsRealPricingAndRequiresSelectionBeforeChanging() {
        var selected by mutableStateOf<Plan?>(null)
        var requested = 0
        compose.setContent {
            SpanishTata {
                PlanSubscriptionScreen(
                    state = PlanSubscriptionUiState(
                        isLoading = false, subscription = subscription,
                        plans = listOf(essential, family), selectedPlan = selected,
                    ),
                    onBack = {}, onRetry = {},
                    onSelectPlan = { selected = it },
                    onChangePlan = { requested++ },
                    onChangeCancel = {}, onChangeConfirm = {},
                )
            }
        }
        compose.onNodeWithText("Plan y suscripción").assertIsDisplayed()
        compose.onNodeWithText("Esencial").assertIsDisplayed()
        compose.onNodeWithText("Familiar").assertIsDisplayed()
        compose.onNodeWithText("Cambiar plan").assertIsNotEnabled()
        compose.waitForIdle()
        capture("tata-plan28-native-20261026.png")
        compose.onNodeWithText("Esencial").performClick()
        compose.onNodeWithText("Cambiar plan").performClick()
        assertEquals("ESSENTIAL", selected?.code)
        assertEquals(1, requested)
    }

    @Test fun changedSubscriptionIsAcknowledgedWithoutClaimingExternalPayment() {
        compose.setContent {
            SpanishTata {
                PlanSubscriptionScreen(
                    state = PlanSubscriptionUiState(
                        isLoading = false, subscription = subscription,
                        plans = listOf(essential, family),
                        changeMessage = PlanChangeMessage.Updated,
                    ),
                    onBack = {}, onRetry = {}, onSelectPlan = {},
                    onChangePlan = {}, onChangeCancel = {}, onChangeConfirm = {},
                )
            }
        }
        compose.onNodeWithText("Suscripción actualizada").performScrollTo().assertIsDisplayed()
        compose.waitForIdle()
        capture("tata-plan68-native-20261026.png")
    }

    @Test fun absentRenewalDataIsNotReplacedWithFigmaExampleDate() {
        compose.setContent {
            SpanishTata {
                PlanSubscriptionScreen(
                    state = PlanSubscriptionUiState(
                        isLoading = false,
                        subscription = subscription.copy(renewsAt = null),
                        plans = listOf(essential, family),
                    ),
                    onBack = {}, onRetry = {}, onSelectPlan = {},
                    onChangePlan = {}, onChangeCancel = {}, onChangeConfirm = {},
                )
            }
        }
        compose.onNodeWithText("Plan y suscripción").assertIsDisplayed()
        compose.onNodeWithText("28 oct.").assertDoesNotExist()
    }
}
