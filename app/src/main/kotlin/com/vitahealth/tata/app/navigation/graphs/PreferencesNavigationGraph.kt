package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openAdultDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityViewModel
import com.vitahealth.tata.preferences.presentation.navigation.preferencesDestinations
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar

/** Connects settings entry points to the application's caregiver tabs and back stack. */
internal fun NavGraphBuilder.preferencesDestinations(
    navController: NavHostController,
    accessibilityFactoryFor: (String) -> AccessibilityViewModel.Factory,
    notificationsFactoryFor: (String) -> NotificationPreferencesViewModel.Factory,
) {
    preferencesDestinations(
        accessibilityFactoryFor = accessibilityFactoryFor,
        notificationsFactoryFor = notificationsFactoryFor,
        onBack = { navController.popBackStack() },
        onTabSelected = { tab ->
            if (tab != CaregiverTab.More) navController.openCaregiverTab(tab)
        },
        accessibilityNavigationBar = { userId ->
            // The back stack selects the navigation shell, never grants data access.
            val adultHome =
                runCatching { navController.getBackStackEntry(RootDestination.NextDoseHome.route) }
                    .getOrNull()
            if (adultHome?.arguments?.getString("olderAdultId") == userId) {
                AdultTabBar(
                    selected = AdultTab.More,
                    availableTabs = AdultTab.entries.toSet(),
                    onSelect = { tab ->
                        when (tab) {
                            AdultTab.Home ->
                                navController.popBackStack(
                                    RootDestination.NextDoseHome.route,
                                    false,
                                )
                            AdultTab.Medications ->
                                navController.openAdultDestination(
                                    RootDestination.MyMedications.createRoute(userId)
                                )
                            AdultTab.Agenda ->
                                navController.openAdultDestination(
                                    RootDestination.IntakeAgenda.createRoute(userId)
                                )
                            AdultTab.Notes ->
                                navController.openAdultDestination(
                                    RootDestination.PersonalNotes.createRoute(userId)
                                )
                            AdultTab.More -> Unit
                        }
                    },
                )
            } else {
                CaregiverTabBar(
                    selected = CaregiverTab.More,
                    onSelect = { tab ->
                        if (tab != CaregiverTab.More) navController.openCaregiverTab(tab)
                    },
                )
            }
        },
    )
}
