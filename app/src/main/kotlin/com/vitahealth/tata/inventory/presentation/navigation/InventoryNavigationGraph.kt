package com.vitahealth.tata.inventory.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.inventory.presentation.inventory.InventoryRoute
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

/**
 * Registers inventory UI while the host supplies dependencies and navigation outside this context.
 */
fun NavGraphBuilder.inventoryDestination(
    factoryFor:
        (medicationId: String, medicationName: String, unit: String) -> InventoryViewModel.Factory,
    olderAdultName: () -> String,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
) {
    composable(
        route = InventoryDestination.route,
        arguments =
            listOf(
                navArgument(InventoryDestination.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(InventoryDestination.medicationNameArgument) {
                    type = NavType.StringType
                },
                navArgument(InventoryDestination.unitArgument) { type = NavType.StringType },
            ),
    ) { entry ->
        val medicationId =
            requireNotNull(entry.arguments?.getString(InventoryDestination.medicationIdArgument))
        val medicationName =
            requireNotNull(entry.arguments?.getString(InventoryDestination.medicationNameArgument))
                .trim()
        val unit =
            requireNotNull(entry.arguments?.getString(InventoryDestination.unitArgument)).trim()
        InventoryRoute(
            factory = factoryFor(medicationId, medicationName, unit),
            olderAdultName = olderAdultName(),
            onSignIn = onSignIn,
            onBack = onBack,
            onTabSelected = onTabSelected,
        )
    }
}
