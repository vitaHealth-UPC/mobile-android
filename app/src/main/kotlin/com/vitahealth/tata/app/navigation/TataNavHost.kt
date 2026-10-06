package com.vitahealth.tata.app.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitahealth.tata.app.TataApplication
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
            CaregiverRegistrationRoute(factory = app.container.caregiverRegistrationViewModelFactory)
        }
    }
}
