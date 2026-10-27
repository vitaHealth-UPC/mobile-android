package com.vitahealth.tata.app.navigation

import androidx.navigation.NavHostController
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

/**
 * Bottom tabs of the follow-up screens. Alerts and Notes open their screen right above the family
 * summary, so switching tabs never stacks screens; the other tabs go back to the summary.
 */
internal fun NavHostController.openCaregiverTab(
    tab: CaregiverTab,
    caregiverId: String,
    olderAdultId: String,
) {
    val route =
        when (tab) {
            CaregiverTab.Alerts -> RootDestination.Alerts.createRoute(caregiverId, olderAdultId)
            CaregiverTab.Notes -> RootDestination.Notes.createRoute(caregiverId, olderAdultId)
            CaregiverTab.Person -> RootDestination.CaregiverProfiles.createRoute(caregiverId)
            else -> null
        }
    if (route == null) {
        popBackStack(RootDestination.FamilySummary.route, inclusive = false)
        return
    }
    navigate(route) {
        popUpTo(RootDestination.FamilySummary.route)
        launchSingleTop = true
    }
}

/**
 * Resolve the caregiver context for detail screens whose route only carries an adult or medication.
 */
internal fun NavHostController.openCaregiverTab(tab: CaregiverTab) {
    val family = runCatching { getBackStackEntry(RootDestination.FamilySummary.route) }.getOrNull()
    val caregiver = family?.arguments?.getString("caregiverId")
    val adult = family?.arguments?.getString("olderAdultId")
    if (caregiver != null && adult != null) openCaregiverTab(tab, caregiver, adult)
    else popBackStack()
}

/** Opens the summary belonging to this card instead of an unrelated previous family stack. */
internal fun NavHostController.openFamilySummary(
    caregiverId: String,
    olderAdultId: String,
    name: String,
) {
    val family = runCatching { getBackStackEntry(RootDestination.FamilySummary.route) }.getOrNull()
    if (
        family?.arguments?.getString("caregiverId") == caregiverId &&
            family.arguments?.getString("olderAdultId") == olderAdultId
    ) {
        popBackStack(RootDestination.FamilySummary.route, inclusive = false)
    } else {
        navigate(RootDestination.FamilySummary.createRoute(caregiverId, olderAdultId, name)) {
            launchSingleTop = true
        }
    }
}
