package com.vitahealth.tata.app.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertSummaryNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cardSummaryWorksWithoutAParentAndNeverReturnsToAnotherAdult() {
        lateinit var nav: NavHostController
        compose.setContent {
            nav = rememberNavController()
            NavHost(navController = nav, startDestination = "alerts-entry") {
                composable("alerts-entry") { Text("Alerts") }
                composable(RootDestination.FamilySummary.route) { Text("Summary") }
            }
        }
        compose.runOnIdle { nav.openFamilySummary("caregiver", "adult-a", "Rosa Vargas") }
        compose.runOnIdle {
            assertEquals(
                "adult-a",
                nav.currentBackStackEntry!!.arguments!!.getString("olderAdultId"),
            )
            assertEquals(
                "Rosa Vargas",
                nav.currentBackStackEntry!!.arguments!!.getString("olderAdultName"),
            )
            nav.navigate("alerts-entry")
        }
        compose.runOnIdle { nav.openFamilySummary("caregiver", "adult-b", "Other Person") }
        compose.runOnIdle {
            assertEquals(
                "adult-b",
                nav.currentBackStackEntry!!.arguments!!.getString("olderAdultId"),
            )
            nav.navigate("alerts-entry")
        }
        compose.runOnIdle { nav.openFamilySummary("caregiver", "adult-b", "Other Person") }
        compose.runOnIdle {
            assertEquals(
                "adult-b",
                nav.currentBackStackEntry!!.arguments!!.getString("olderAdultId"),
            )
            assertEquals(
                "caregiver",
                nav.currentBackStackEntry!!.arguments!!.getString("caregiverId"),
            )
        }
    }
}
