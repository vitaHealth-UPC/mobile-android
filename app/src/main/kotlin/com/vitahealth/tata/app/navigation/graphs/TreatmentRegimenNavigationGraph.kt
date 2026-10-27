package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentScheduleInstructionsRoute
import java.time.format.DateTimeFormatter

/**
 * Registers treatment creation and regimen destinations while the app host owns cross-context
 * navigation.
 */
internal fun NavGraphBuilder.treatmentRegimenDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        route = RootDestination.TreatmentCreation.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentCreation.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentCreation.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentCreation.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentCreation.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentCreation.medicationLabelArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentCreation.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentCreation.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentCreation.olderAdultNameArgument
                )
            )
        val medicationId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentCreation.medicationIdArgument
                )
            )
        val medicationLabel =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentCreation.medicationLabelArgument
                )
            )

        TreatmentCreationRoute(
            factory =
                container.treatment.treatmentCreationViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                ),
            onCreated = { treatment ->
                navController.navigate(
                    RootDestination.TreatmentDoseFrequency.createRoute(
                        caregiverId = caregiverId,
                        olderAdultId = olderAdultId,
                        olderAdultName = olderAdultName,
                        medicationId = medicationId,
                        medicationLabel = medicationLabel,
                        treatmentId = treatment.id,
                        treatmentName = treatment.name,
                    )
                ) {
                    popUpTo(RootDestination.TreatmentCreation.route) { inclusive = true }
                }
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set(
                        "draftFrequency",
                        backStackEntry.savedStateHandle.get<String>("draftFrequency") ?: "",
                    )
                    set(
                        "draftInstructions",
                        backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "",
                    )
                }
            },
        )
    }

    composable(
        route = RootDestination.TreatmentDoseFrequency.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentDoseFrequency.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationLabelArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentNameArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.olderAdultNameArgument
                )
            )
        val medicationId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.medicationIdArgument
                )
            )
        val medicationLabel =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.medicationLabelArgument
                )
            )
        val treatmentId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.treatmentIdArgument
                )
            )
        val treatmentName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentDoseFrequency.treatmentNameArgument
                )
            )

        TreatmentDoseFrequencyRoute(
            initialFrequency = backStackEntry.savedStateHandle.get<String>("draftFrequency") ?: "",
            factory =
                container.treatment.treatmentDoseFrequencyViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    draftState = treatmentWizardDraftState(navController, backStackEntry),
                ),
            onCompleted = { basics ->
                navController.navigate(
                    RootDestination.TreatmentScheduleInstructions.createRoute(
                        caregiverId = caregiverId,
                        olderAdultId = olderAdultId,
                        olderAdultName = olderAdultName,
                        medicationId = medicationId,
                        medicationLabel = medicationLabel,
                        treatmentId = treatmentId,
                        treatmentName = treatmentName,
                        dosage = basics.dosage.value,
                        frequency = basics.frequency.value,
                    )
                ) {
                    launchSingleTop = true
                }
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(
                        "draftInstructions",
                        backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "",
                    )
            },
        )
    }

    composable(
        route = RootDestination.TreatmentScheduleInstructions.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentScheduleInstructions.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationLabelArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.dosageArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentScheduleInstructions.frequencyArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument
                )
            )
        val medicationId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.medicationIdArgument
                )
            )
        val medicationLabel =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.medicationLabelArgument
                )
            )
        val treatmentId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.treatmentIdArgument
                )
            )
        val treatmentName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.treatmentNameArgument
                )
            )
        val dosage =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.dosageArgument
                )
            )
        val frequency =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentScheduleInstructions.frequencyArgument
                )
            )

        TreatmentScheduleInstructionsRoute(
            initialInstructions =
                backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "",
            factory =
                container.treatment.treatmentScheduleInstructionsViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    dosage = dosage,
                    frequency = frequency,
                    draftState = treatmentWizardDraftState(navController, backStackEntry),
                ),
            onCompleted = { schedule ->
                val scheduleText =
                    schedule.schedule.times.joinToString(",") {
                        it.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }
                navController.navigate(
                    RootDestination.TreatmentReminders.createRoute(
                        caregiverId = caregiverId,
                        olderAdultId = olderAdultId,
                        olderAdultName = olderAdultName,
                        medicationId = medicationId,
                        medicationLabel = medicationLabel,
                        treatmentId = treatmentId,
                        treatmentName = treatmentName,
                        dosage = dosage,
                        frequency = frequency,
                        scheduleText = scheduleText,
                        instructions = schedule.instructions.value,
                    )
                ) {
                    launchSingleTop = true
                }
            },
        )
    }
}
