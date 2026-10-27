package com.vitahealth.tata.intake.presentation.home

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import com.vitahealth.tata.app.withSpanishAppLocale
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.home.screens.NextDoseHomeScreen
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Composable
private fun SpanishTata(content: @Composable () -> Unit) {
    val base = LocalContext.current
    val spanish = remember(base) { base.withSpanishAppLocale() }
    CompositionLocalProvider(LocalContext provides spanish) {
        TataTheme { content() }
    }
}

@RunWith(AndroidJUnit4::class)
class ReminderDueFigmaAcceptanceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun dose(status: DoseStatus) = NextDoseReadModel(
        id = "intake-39", treatmentId = "regimen-39",
        medicationId = "medication-39", olderAdultId = "adult-39",
        medicationName = "Losartán 50 mg", dose = "1 tableta",
        instructions = "", scheduledAt = Instant.now().minusSeconds(60),
        status = status,
    )

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        Thread.sleep(750)
        val frame = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val output = requireNotNull(instrumentation.targetContext.getExternalFilesDir(null))
            .resolve(name)
        output.outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
        frame.recycle()
    }

    @Test fun pendingDueDoseRendersReminderAndPreservesNavigableDose() {
        var opened = ""
        var fixture by mutableStateOf<NextDoseHomeUiState>(NextDoseHomeUiState.Loading)
        compose.setContent {
            SpanishTata {
                NextDoseHomeScreen(
                    fixture,
                    onRetry = {},
                    onOpenDoseDetail = { opened = it },
                    onOpenAgenda = {},
                )
            }
        }
        compose.onNodeWithText("Consultando tu programación…").assertIsDisplayed()
        compose.runOnIdle {
            fixture = NextDoseHomeUiState.NextDoseAvailable(
                dose = dose(DoseStatus.PENDING), olderAdultName = "Rosa Vargas",
            )
        }
        compose.onNodeWithText("Es hora de tu toma").assertIsDisplayed()
        compose.onNodeWithText("Losartán 50 mg").assertIsDisplayed()
        compose.onNodeWithText("Losartán 50 mg").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Consejo del día").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hoy").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Es hora de tu toma").assertIsDisplayed()
        capture("tata-reminder39-state-transition-20261026.png")
        compose.onNodeWithText("Losartán 50 mg").performClick()
        assertEquals("intake-39", opened)
    }

    @Test fun futurePendingDoseBecomesDueWhileHomeRemainsOpen() {
        // Compose controls coroutine delays. The supplied clock must advance on that same timeline.
        compose.mainClock.autoAdvance = false
        val origin = Instant.parse("2026-10-26T12:00:00Z")
        var fixture by mutableStateOf<NextDoseHomeUiState>(NextDoseHomeUiState.Loading)
        compose.setContent {
            SpanishTata {
                NextDoseHomeScreen(
                    fixture, onRetry = {}, onOpenDoseDetail = {}, onOpenAgenda = {},
                    currentTime = { origin.plusMillis(compose.mainClock.currentTime) },
                )
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Consultando tu programación…").assertIsDisplayed()
        compose.runOnIdle {
            fixture = NextDoseHomeUiState.NextDoseAvailable(
                dose = dose(DoseStatus.PENDING).copy(
                    scheduledAt = origin.plusMillis(compose.mainClock.currentTime + 6_000),
                ),
                olderAdultName = "Rosa Vargas",
            )
        }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Es hora de tu toma").assertDoesNotExist()
        compose.mainClock.advanceTimeBy(6_100)
        compose.waitForIdle()
        compose.onNodeWithText("Es hora de tu toma").assertIsDisplayed()
        compose.runOnIdle {
            fixture = NextDoseHomeUiState.NextDoseAvailable(
                dose = dose(DoseStatus.CONFIRMED), olderAdultName = "Rosa Vargas",
            )
        }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Es hora de tu toma").assertDoesNotExist()
    }

    @Test fun resolvedDoseDoesNotShowDueReminder() {
        compose.setContent {
            SpanishTata {
                NextDoseHomeScreen(
                    NextDoseHomeUiState.NextDoseAvailable(
                        dose = dose(DoseStatus.CONFIRMED), olderAdultName = "Rosa Vargas",
                    ),
                    onRetry = {}, onOpenDoseDetail = {}, onOpenAgenda = {},
                )
            }
        }
        compose.onNodeWithText("Es hora de tu toma").assertDoesNotExist()
    }
}
