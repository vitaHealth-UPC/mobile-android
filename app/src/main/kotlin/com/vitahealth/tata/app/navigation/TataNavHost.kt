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
            )
        }
    }
}
