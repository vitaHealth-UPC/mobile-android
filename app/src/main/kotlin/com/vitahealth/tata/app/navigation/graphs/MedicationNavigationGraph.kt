package com.vitahealth.tata.app.navigation.graphs

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openAdultDestination
import com.vitahealth.tata.treatment.presentation.medication.MedicationManagementRoute
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationRoute
import com.vitahealth.tata.treatment.presentation.medication.MyMedicationsRoute
import kotlinx.coroutines.launch

/** Registers the treatment context's medication destinations in the host graph. */
internal fun NavGraphBuilder.medicationDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        route = RootDestination.MedicationManagement.route,
        arguments =
            listOf(
                navArgument(RootDestination.MedicationManagement.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.MedicationManagement.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.MedicationManagement.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.MedicationManagement.deactivateMedicationIdArgument) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationManagement.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationManagement.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationManagement.olderAdultNameArgument
                )
            )
        val requestedDeactivation =
            backStackEntry.arguments?.getString(
                RootDestination.MedicationManagement.deactivateMedicationIdArgument
            )?.takeIf(String::isNotBlank)
        MedicationManagementRoute(
            initialDeactivationMedicationId = requestedDeactivation,
            factory =
                container.treatment.medicationManagementViewModelFactory(
                    caregiverId,
                    olderAdultId,
                    olderAdultName,
                ),
            onBack = { navController.popBackStack() },
            onAddMedication = {
                navController.navigate(
                    RootDestination.MedicationRegistration.createRoute(
                        caregiverId,
                        olderAdultId,
                        olderAdultName,
                    )
                )
            },
        )
    }

    composable(
        route = RootDestination.MedicationRegistration.route,
        arguments =
            listOf(
                navArgument(RootDestination.MedicationRegistration.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.MedicationRegistration.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.MedicationRegistration.olderAdultNameArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationRegistration.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationRegistration.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.MedicationRegistration.olderAdultNameArgument
                )
            )

        MedicationRegistrationRoute(
            onBack = { navController.popBackStack() },
            factory =
                container.treatment.medicationRegistrationViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
            onRegistered = { medication, draft ->
                navController.navigate(
                    RootDestination.TreatmentCreation.createRoute(
                        caregiverId = caregiverId,
                        olderAdultId = olderAdultId,
                        olderAdultName = olderAdultName,
                        medicationId = medication.id,
                        medicationLabel = medication.name + " · " + medication.presentation,
                    )
                ) {
                    popUpTo(RootDestination.MedicationRegistration.route) { inclusive = true }
                }
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("draftFrequency", draft.frequency)
                    set(
                        "draftInstructions",
                        listOf(draft.timing, draft.notes)
                            .filter { it.isNotBlank() }
                            .joinToString("\n"),
                    )
                }
            },
        )
    }

    composable(
        route = RootDestination.MyMedications.route,
        arguments = listOf(navArgument("olderAdultId") { type = NavType.StringType }),
    ) { entry ->
        val olderAdultId = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val scope = rememberCoroutineScope()
        MyMedicationsRoute(
            factory = container.myMedicationsViewModelFactory(olderAdultId),
            onSignIn = {
                scope.launch {
                    container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
            onHome = { navController.popBackStack(RootDestination.NextDoseHome.route, false) },
            onAgenda = {
                navController.openAdultDestination(
                    RootDestination.IntakeAgenda.createRoute(olderAdultId)
                )
            },
            onNotes = {
                navController.openAdultDestination(
                    RootDestination.PersonalNotes.createRoute(olderAdultId)
                )
            },
            onOpenDose = { navController.navigate(RootDestination.DoseDetail.createRoute(it)) },
        )
    }
}
