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
                navArgument(RootDestination.CareLink.caregiverIdArgument) {
                    type = NavType.StringType
                },
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
                        popUpTo(RootDestination.CareLink.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = RootDestination.MedicationRegistration.route,
            arguments = listOf(
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
            )
        }
    }
}
