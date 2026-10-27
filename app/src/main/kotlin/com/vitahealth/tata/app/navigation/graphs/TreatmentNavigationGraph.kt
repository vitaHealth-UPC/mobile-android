package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination

/**
 * Composes all Treatment & Medication routes, retaining the existing host
 * destinations and back-stack behavior. Feature UI stays within Treatment BC.
 */
internal fun NavGraphBuilder.treatmentDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    treatmentCatalogDestinations(navController, container)
    medicationDestinations(navController, container)
    treatmentWizardDestinations(navController, container)
}
