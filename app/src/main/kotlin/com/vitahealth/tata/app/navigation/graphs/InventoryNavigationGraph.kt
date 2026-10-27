package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import com.vitahealth.tata.inventory.presentation.navigation.inventoryDestination

/**
 * Connects inventory's entry point to account recovery and application-wide caregiver navigation.
 */
internal fun NavGraphBuilder.inventoryDestinations(
    navController: NavHostController,
    factoryFor: (String, String, String) -> InventoryViewModel.Factory,
) {
    inventoryDestination(
        factoryFor = factoryFor,
        olderAdultName = {
            runCatching { navController.getBackStackEntry(RootDestination.FamilySummary.route) }
                .getOrNull()
                ?.arguments
                ?.getString("olderAdultName") ?: ""
        },
        onSignIn = {
            navController.navigate(RootDestination.SessionAccess.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onBack = { navController.popBackStack() },
        onTabSelected = { navController.openCaregiverTab(it) },
    )
}
