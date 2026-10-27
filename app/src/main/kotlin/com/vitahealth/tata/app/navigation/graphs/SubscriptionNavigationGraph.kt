package com.vitahealth.tata.app.navigation.graphs

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.identity.presentation.subscription.PlanSubscriptionRoute
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import kotlinx.coroutines.launch

/** Registers subscriptionDestinations in the shared parent graph with unchanged route contracts. */
internal fun NavGraphBuilder.subscriptionDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        route = RootDestination.PlanSubscription.route,
        arguments =
            listOf(
                navArgument(RootDestination.PlanSubscription.accountIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { backStackEntry ->
        val accountId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.PlanSubscription.accountIdArgument
                )
            )
        val scope = rememberCoroutineScope()
        PlanSubscriptionRoute(
            factory = container.planSubscriptionViewModelFactory(accountId),
            onBack = { navController.popBackStack() },
            onSignIn = {
                scope.launch {
                    container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            onTabSelected = { tab ->
                if (tab != CaregiverTab.More) {
                    val family =
                        runCatching {
                                navController.getBackStackEntry(RootDestination.FamilySummary.route)
                            }
                            .getOrNull()
                    val caregiver = family?.arguments?.getString("caregiverId")
                    val adult = family?.arguments?.getString("olderAdultId")
                    if (caregiver != null && adult != null) {
                        if (tab == CaregiverTab.Person)
                            navController.navigate(
                                RootDestination.CaregiverProfiles.createRoute(caregiver)
                            )
                        else navController.openCaregiverTab(tab, caregiver, adult)
                    } else navController.popBackStack()
                }
            },
        )
    }
}
