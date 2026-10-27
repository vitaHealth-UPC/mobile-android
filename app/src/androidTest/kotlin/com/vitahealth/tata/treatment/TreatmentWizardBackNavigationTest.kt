package com.vitahealth.tata.treatment

import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.graphs.treatmentWizardDraftState
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommandHandler
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyRoute
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDoseFrequencyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class TreatmentWizardBackNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun goingBackKeepsTheEnteredPautaAndDoesNotImmediatelyNavigateForwardAgain() {
        lateinit var nav: NavHostController
        var completions = 0
        compose.setContent {
            nav = rememberNavController()
            NavHost(navController = nav, startDestination = "dose") {
                composable("dose") {
                    TreatmentDoseFrequencyRoute(
                        TreatmentDoseFrequencyViewModel.Factory(
                            "caregiver",
                            "adult",
                            "Rosa",
                            "medication",
                            "Losartan",
                            "treatment",
                            "Control",
                            SetDoseFrequencyCommandHandler(),
                        ),
                        onCompleted = {
                            completions++
                            nav.navigate("next")
                        },
                    )
                }
                composable("next") { Text("Paso siguiente") }
            }
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("2 tabletas")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("Diaria")
        compose.onNodeWithText("Siguiente").performScrollTo().performClick()
        compose.onNodeWithText("Paso siguiente").assertIsDisplayed()
        compose.runOnIdle { nav.popBackStack() }
        compose.onNodeWithText("2 tabletas").assertExists()
        compose.onNodeWithText("Diaria").assertExists()
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals("dose", nav.currentDestination?.route)
            assertEquals(1, completions)
        }
        compose.onNodeWithText("Siguiente").performScrollTo().performClick()
        compose.onNodeWithText("Paso siguiente").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, completions) }
    }

    @Test
    fun draftScopeSharesOnlyMatchingIdentityAndSupportsDirectEntry() {
        lateinit var nav: NavHostController
        lateinit var selected: SavedStateHandle
        val root = RootDestination.TreatmentDoseFrequency
        compose.setContent {
            nav = rememberNavController()
            NavHost(
                navController = nav,
                startDestination =
                    root.createRoute(
                        "caregiver",
                        "adult",
                        "Rosa",
                        "medication",
                        "Losartan",
                        "treatment",
                        "Control",
                    ),
            ) {
                composable(root.route) { entry ->
                    selected = treatmentWizardDraftState(nav, entry)
                    Text("Pauta")
                }
                composable("review/{caregiverId}/{olderAdultId}/{medicationId}/{treatmentId}") {
                    entry ->
                    selected = treatmentWizardDraftState(nav, entry)
                    Text("Revision")
                }
            }
        }
        lateinit var original: SavedStateHandle
        compose.runOnIdle {
            original = selected
            original["scheduleText"] = "08:15,20:15"
            nav.navigate("review/caregiver/adult/medication/treatment")
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertSame(original, selected)
            assertEquals("08:15,20:15", selected.get<String>("scheduleText"))
            nav.navigate("review/caregiver/other-adult/medication/other-treatment")
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertNull(selected.get<String>("scheduleText"))
            nav.navigate("review/caregiver/direct-adult/medication/direct-treatment") {
                popUpTo(root.route) { inclusive = true }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { assertNull(selected.get<String>("scheduleText")) }
    }
}
