package com.vitahealth.tata.preferences.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.preferences.presentation.accessibility.AccessibilityRoute
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityViewModel
import com.vitahealth.tata.preferences.presentation.notifications.NotificationPreferencesRoute
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

/** Registers settings screens while the host owns navigation outside this context. */
fun NavGraphBuilder.preferencesDestinations(
    accessibilityFactoryFor: (String) -> AccessibilityViewModel.Factory,
    notificationsFactoryFor: (String) -> NotificationPreferencesViewModel.Factory,
    onBack: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
    accessibilityNavigationBar: @Composable (String) -> Unit = {},
) {
    composable(
        route = NotificationPreferencesDestination.route,
        arguments =
            listOf(
                navArgument(NotificationPreferencesDestination.userIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { entry ->
        val userId =
            requireNotNull(
                entry.arguments?.getString(NotificationPreferencesDestination.userIdArgument)
            )
        NotificationPreferencesRoute(
            factory = notificationsFactoryFor(userId),
            onBack = onBack,
            onTabSelected = onTabSelected,
        )
    }
    composable(
        route = AccessibilityDestination.route,
        arguments =
            listOf(
                navArgument(AccessibilityDestination.userIdArgument) { type = NavType.StringType }
            ),
    ) { entry ->
        val userId =
            requireNotNull(entry.arguments?.getString(AccessibilityDestination.userIdArgument))
        AccessibilityRoute(
            factory = accessibilityFactoryFor(userId),
            onBack = onBack,
            navigationBar = { accessibilityNavigationBar(userId) },
        )
    }
}
