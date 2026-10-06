package com.vitahealth.tata.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitahealth.tata.app.TataApplication
import com.vitahealth.tata.carelink.presentation.link.CareLinkRoute
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationRoute
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyRoute

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
            )
        }
    }
}
