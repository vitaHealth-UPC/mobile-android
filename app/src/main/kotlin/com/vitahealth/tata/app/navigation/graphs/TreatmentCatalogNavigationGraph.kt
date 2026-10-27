package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.inventory.presentation.navigation.InventoryDestination
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentListRoute

/** Registers the treatment context's treatmentCatalog destinations in the host graph. */
internal fun NavGraphBuilder.treatmentCatalogDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        RootDestination.TreatmentList.route,
        arguments =
            listOf(
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("olderAdultId") { type = NavType.StringType },
                navArgument("olderAdultName") { type = NavType.StringType },
            ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val name = requireNotNull(entry.arguments?.getString("olderAdultName"))
        TreatmentListRoute(
            container.treatment.treatmentListViewModelFactory(caregiver, adult),
            name,
            onOpen = { id ->
                navController.navigate(
                    RootDestination.TreatmentDetail.createRoute(caregiver, name, id, "")
                )
            },
            onAddMedication = {
                navController.navigate(
                    RootDestination.MedicationRegistration.createRoute(caregiver, adult, name)
                )
            },
            onBack = { navController.popBackStack() },
        )
    }

    composable(
        route = RootDestination.TreatmentDetail.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentDetail.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDetail.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDetail.treatmentIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDetail.medicationLabelHintArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDetail.caregiverIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDetail.olderAdultNameArgument
                )
            )
        val treatmentId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDetail.treatmentIdArgument
                )
            )
        val medicationLabelHint =
            requireNotNull(
                    backStackEntry.arguments?.getString(
                        RootDestination.TreatmentDetail.medicationLabelHintArgument
                    )
                )
                .trim()

        fun returnToProfiles() {
            navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiverId)) {
                popUpTo(backStackEntry.destination.id) { inclusive = true }
                launchSingleTop = true
            }
        }

        TreatmentDetailRoute(
            factory =
                container.treatment.treatmentDetailViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultName = olderAdultName,
                    treatmentId = treatmentId,
                    medicationLabelHint = medicationLabelHint,
                ),
            onBack = { navController.popBackStack() },
            onRestrictedBack = ::returnToProfiles,
            navigationBar = {
                CaregiverTabBar(
                    CaregiverTab.Person,
                    { tab ->
                        val family =
                            runCatching {
                                    navController.getBackStackEntry(
                                        RootDestination.FamilySummary.route
                                    )
                                }
                                .getOrNull()
                        val adult = family?.arguments?.getString("olderAdultId")
                        if (
                            tab == CaregiverTab.Person ||
                                adult == null ||
                                family.arguments?.getString("caregiverId") != caregiverId
                        )
                            returnToProfiles()
                        else navController.openCaregiverTab(tab, caregiverId, adult)
                    },
                )
            },
            onManageTreatment = { detail, label ->
                navController.openTreatmentManagement(caregiverId, olderAdultName, detail, label)
            },
            onOpenInventory = { medicationId, medicationName ->
                navController.navigate(
                    InventoryDestination.createRoute(
                        medicationId = medicationId,
                        medicationName = medicationName,
                        unit = "",
                    )
                )
            },
        )
    }
}
