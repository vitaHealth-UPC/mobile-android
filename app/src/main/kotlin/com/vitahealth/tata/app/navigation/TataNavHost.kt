package com.vitahealth.tata.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitahealth.tata.analytics.presentation.history.AdherenceHistoryRoute
import com.vitahealth.tata.app.TataApplication
import com.vitahealth.tata.carelink.presentation.link.CareLinkRoute
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationRoute
import com.vitahealth.tata.intake.presentation.detail.DoseDetailRoute
import com.vitahealth.tata.intake.presentation.home.NextDoseHomeRoute
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentScheduleInstructionsRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentReminderRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailRoute
import java.time.format.DateTimeFormatter

@Composable
fun TataNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RootDestination.CaregiverRegistration.route,
        modifier = modifier,
    ) {
        composable(RootDestination.CaregiverRegistration.route) {
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            CaregiverRegistrationRoute(
                factory = app.container.caregiverRegistrationViewModelFactory,
                onRegistrationComplete = { caregiverId ->
                    navController.navigate(RootDestination.CareLink.createRoute(caregiverId)) {
                        popUpTo(RootDestination.CaregiverRegistration.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = RootDestination.CareLink.route,
            arguments = listOf(
                navArgument(RootDestination.CareLink.caregiverIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.CareLink.caregiverIdArgument),
            )
            CareLinkRoute(
                factory = app.container.careLinkViewModelFactory(caregiverId),
                onConfirmed = { olderAdultId, olderAdultName ->
                    navController.navigate(
                        RootDestination.MedicationRegistration.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                        ),
                    ) {
                        popUpTo(RootDestination.CareLink.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.MedicationRegistration.route,
            arguments = listOf(
                navArgument(RootDestination.MedicationRegistration.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationRegistration.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationRegistration.olderAdultNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.olderAdultNameArgument),
            )

            MedicationRegistrationRoute(
                factory = app.container.medicationRegistrationViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
                onRegistered = { medication ->
                    navController.navigate(
                        RootDestination.TreatmentCreation.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medication.id,
                            medicationLabel = medication.name + " · " + medication.presentation,
                        ),
                    ) {
                        popUpTo(RootDestination.MedicationRegistration.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentCreation.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentCreation.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.medicationLabelArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.medicationLabelArgument),
            )

            TreatmentCreationRoute(
                factory = app.container.treatmentCreationViewModelFactory(
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
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentCreation.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentDoseFrequency.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentDoseFrequency.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.treatmentNameArgument),
            )

            TreatmentDoseFrequencyRoute(
                factory = app.container.treatmentDoseFrequencyViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
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
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentDoseFrequency.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentScheduleInstructions.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentScheduleInstructions.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.frequencyArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.frequencyArgument),
            )

            TreatmentScheduleInstructionsRoute(
                factory = app.container.treatmentScheduleInstructionsViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    dosage = dosage,
                    frequency = frequency,
                ),
                onCompleted = { schedule ->
                    val scheduleText = schedule.schedule.times.joinToString(",") {
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
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentScheduleInstructions.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentReminders.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentReminders.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.frequencyArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.scheduleTextArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.instructionsArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.frequencyArgument),
            )
            val scheduleText = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.scheduleTextArgument),
            )
            val instructions = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.instructionsArgument),
            ).trim()

            TreatmentReminderRoute(
                factory = app.container.treatmentReminderViewModelFactory(
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
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentReminders.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentLifecycle.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentLifecycle.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.frequencyArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.scheduleTextArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.instructionsArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.reminderDelayArgument) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.frequencyArgument),
            )
            val scheduleText = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.scheduleTextArgument),
            )
            val instructions = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.instructionsArgument),
            ).trim()
            val reminderDelay = requireNotNull(
                backStackEntry.arguments?.getInt(RootDestination.TreatmentLifecycle.reminderDelayArgument),
            )

            TreatmentLifecycleRoute(
                factory = app.container.treatmentLifecycleViewModelFactory(
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
                ),
                onOpenDetail = {
                    navController.navigate(
                        RootDestination.TreatmentDetail.createRoute(
                            caregiverId = caregiverId,
                            olderAdultName = olderAdultName,
                            treatmentId = treatmentId,
                            medicationLabelHint = medicationLabel,
                        ),
                    )
                },
            )
        }

        composable(
            route = RootDestination.TreatmentDetail.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentDetail.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.medicationLabelHintArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.caregiverIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.olderAdultNameArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.treatmentIdArgument),
            )
            val medicationLabelHint = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.medicationLabelHintArgument),
            ).trim()

            TreatmentDetailRoute(
                factory = app.container.treatmentDetailViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultName = olderAdultName,
                    treatmentId = treatmentId,
                    medicationLabelHint = medicationLabelHint,
                ),
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = RootDestination.NextDoseHome.route,
            arguments = listOf(
                navArgument(RootDestination.NextDoseHome.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.NextDoseHome.olderAdultNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.NextDoseHome.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.NextDoseHome.olderAdultNameArgument),
            )

            NextDoseHomeRoute(
                factory = app.container.nextDoseHomeViewModelFactory(
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
                onOpenDoseDetail = { intakeId ->
                    navController.navigate(RootDestination.DoseDetail.createRoute(intakeId))
                },
            )
        }

        composable(
            route = RootDestination.DoseDetail.route,
            arguments = listOf(
                navArgument(RootDestination.DoseDetail.intakeIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val intakeId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.DoseDetail.intakeIdArgument),
            )

            DoseDetailRoute(
                factory = app.container.doseDetailViewModelFactory(intakeId),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = RootDestination.AdherenceHistory.route,
            arguments = listOf(
                navArgument(RootDestination.AdherenceHistory.olderAdultIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.AdherenceHistory.olderAdultIdArgument),
            )

            AdherenceHistoryRoute(
                factory = app.container.adherenceHistoryViewModelFactory(olderAdultId),
            )
        }

    }
}
