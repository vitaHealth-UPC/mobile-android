package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination

/** Composes regimen and lifecycle destinations without owning their UI logic. */
internal fun NavGraphBuilder.treatmentWizardDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    treatmentRegimenDestinations(navController, container)
    treatmentActivationDestinations(navController, container)
}
