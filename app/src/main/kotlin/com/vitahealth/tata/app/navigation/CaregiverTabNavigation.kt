package com.vitahealth.tata.app.navigation

import androidx.navigation.NavHostController
import com.vitahealth.tata.shared.design.components.CaregiverTab

/**
 * Bottom tabs of the follow-up screens. Alerts and Notes open their screen right above the family summary,
 * so switching tabs never stacks screens; the other tabs go back to the summary.
 */
internal fun NavHostController.openCaregiverTab(tab: CaregiverTab, caregiverId: String, olderAdultId: String) {
    val route = when (tab) {
        CaregiverTab.Alerts -> RootDestination.Alerts.createRoute(caregiverId, olderAdultId)
        CaregiverTab.Notes -> RootDestination.Notes.createRoute(caregiverId, olderAdultId)
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
