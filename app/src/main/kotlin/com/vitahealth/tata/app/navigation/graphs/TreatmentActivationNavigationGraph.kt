package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentReminderRoute

/**
 * Registers reminder and lifecycle destinations while the app host owns cross-context navigation.
 */
internal fun NavGraphBuilder.treatmentActivationDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        route = RootDestination.TreatmentReminders.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentReminders.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.medicationLabelArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.treatmentIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.treatmentNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.dosageArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.frequencyArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.scheduleTextArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentReminders.instructionsArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.olderAdultNameArgument
                )
            )
        val medicationId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.medicationIdArgument
                )
            )
        val medicationLabel =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.medicationLabelArgument
                )
            )
        val treatmentId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.treatmentIdArgument
                )
            )
        val treatmentName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.treatmentNameArgument
                )
            )
        val dosage =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.dosageArgument
                )
            )
        val frequency =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.frequencyArgument
                )
            )
        val scheduleText =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentReminders.scheduleTextArgument
                )
            )
        val instructions =
            requireNotNull(
                    backStackEntry.arguments?.getString(
                        RootDestination.TreatmentReminders.instructionsArgument
                    )
                )
                .trim()

        TreatmentReminderRoute(
            factory =
                container.treatment.treatmentReminderViewModelFactory(
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
                    instructions = instructions,
                    draftState = treatmentWizardDraftState(navController, backStackEntry),
                ),
            onCompleted = { policy ->
                navController.navigate(
                    RootDestination.TreatmentLifecycle.createRoute(
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
                        instructions = instructions,
                        reminderDelay = policy.followUpDelayMinutes.value,
                    )
                ) {
                    launchSingleTop = true
                }
            },
        )
    }

    composable(
        route = RootDestination.TreatmentLifecycle.route,
        arguments =
            listOf(
                navArgument(RootDestination.TreatmentLifecycle.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.medicationIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.medicationLabelArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.treatmentIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.treatmentNameArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.dosageArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.frequencyArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.scheduleTextArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.instructionsArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.TreatmentLifecycle.initialStatusArgument) {
                    type = NavType.StringType
                    defaultValue = TreatmentStatus.DRAFT.name
                },
                navArgument(RootDestination.TreatmentLifecycle.reminderDelayArgument) {
                    type = NavType.IntType
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.caregiverIdArgument
                )
            )
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.olderAdultNameArgument
                )
            )
        val medicationId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.medicationIdArgument
                )
            )
        val medicationLabel =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.medicationLabelArgument
                )
            )
        val treatmentId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.treatmentIdArgument
                )
            )
        val treatmentName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.treatmentNameArgument
                )
            )
        val dosage =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.dosageArgument
                )
            )
        val frequency =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.frequencyArgument
                )
            )
        val scheduleText =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.TreatmentLifecycle.scheduleTextArgument
                )
            )
        val instructions =
            requireNotNull(
                    backStackEntry.arguments?.getString(
                        RootDestination.TreatmentLifecycle.instructionsArgument
                    )
                )
                .trim()
        val reminderDelay =
            requireNotNull(
                backStackEntry.arguments?.getInt(
                    RootDestination.TreatmentLifecycle.reminderDelayArgument
                )
            )

        val initialStatus =
            TreatmentStatus.entries.firstOrNull {
                it.name ==
                    backStackEntry.arguments?.getString(
                        RootDestination.TreatmentLifecycle.initialStatusArgument
                    )
            } ?: TreatmentStatus.DRAFT

        TreatmentLifecycleRoute(
            onCompleteConfiguration = {
                navController.completeTreatmentConfiguration(backStackEntry)
            },
            factory =
                container.treatment.treatmentLifecycleViewModelFactory(
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
                    instructions = instructions,
                    reminderDelayMinutes = reminderDelay,
                    initialStatus = initialStatus,
                ),
            onManageMedication = {
                navController.navigate(
                    RootDestination.MedicationManagement.createRoute(
                        caregiverId = caregiverId,
                        olderAdultId = olderAdultId,
                        olderAdultName = olderAdultName,
                        deactivateMedicationId = medicationId,
                    )
                )
            },
            navigationBar = { canNavigate ->
                CaregiverTabBar(
                    selected = CaregiverTab.Person,
                    onSelect = { tab ->
                        if (canNavigate)
                            navController.openCaregiverTab(tab, caregiverId, olderAdultId)
                    },
                )
            },
            onOpenDetail = {
                if (initialStatus != TreatmentStatus.DRAFT) {
                    navController.popBackStack()
                } else
                    navController.navigate(
                        RootDestination.TreatmentDetail.createRoute(
                            caregiverId = caregiverId,
                            olderAdultName = olderAdultName,
                            treatmentId = treatmentId,
                            medicationLabelHint = medicationLabel,
                        )
                    ) {
                        // Once activated, editable draft entries must not reappear behind the
                        // detail.
                        val draft =
                            runCatching {
                                    navController.getBackStackEntry(
                                        RootDestination.TreatmentDoseFrequency.route
                                    )
                                }
                                .getOrNull()
                        if (
                            draft != null &&
                                draft.arguments?.getString("treatmentId") == treatmentId
                        ) {
                            popUpTo(RootDestination.TreatmentDoseFrequency.route) {
                                inclusive = true
                            }
                        } else {
                            popUpTo(backStackEntry.destination.id) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
            },
        )
    }
}
