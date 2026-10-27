package com.vitahealth.tata.analytics.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.analytics.presentation.history.AdherenceHistoryRoute
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryViewModel
import com.vitahealth.tata.analytics.presentation.recommendations.AdherenceRecommendationsRoute
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

/** Registers analytics screens and their internal history-to-recommendations navigation. */
fun NavGraphBuilder.analyticsDestinations(
    navController: NavHostController,
    historyFactoryFor: (String) -> AdherenceHistoryViewModel.Factory,
    recommendationsFactoryFor: (String) -> AdherenceRecommendationsViewModel.Factory,
    onTabSelected: (CaregiverTab) -> Unit,
    onApplyAdjustments: (String) -> Unit,
) {
    composable(
        route = AdherenceHistoryDestination.route,
        arguments =
            listOf(
                navArgument(AdherenceHistoryDestination.olderAdultIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { backStackEntry ->
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    AdherenceHistoryDestination.olderAdultIdArgument
                )
            )

        AdherenceHistoryRoute(
            factory = historyFactoryFor(olderAdultId),
            onOpenRecommendations = {
                navController.navigate(
                    AdherenceRecommendationsDestination.createRoute(olderAdultId)
                )
            },
            onTabSelected = onTabSelected,
        )
    }

    composable(
        route = AdherenceRecommendationsDestination.route,
        arguments =
            listOf(
                navArgument(AdherenceRecommendationsDestination.olderAdultIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { backStackEntry ->
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    AdherenceRecommendationsDestination.olderAdultIdArgument
                )
            )

        AdherenceRecommendationsRoute(
            factory = recommendationsFactoryFor(olderAdultId),
            onBackToHistory = { navController.popBackStack() },
            onApplyAdjustments = { onApplyAdjustments(olderAdultId) },
            onTabSelected = onTabSelected,
        )
    }
}
