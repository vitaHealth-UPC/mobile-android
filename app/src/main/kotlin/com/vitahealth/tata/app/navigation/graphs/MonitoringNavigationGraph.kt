package com.vitahealth.tata.app.navigation.graphs

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.analytics.presentation.navigation.AdherenceHistoryDestination
import com.vitahealth.tata.app.integration.linkedperson.LinkedPersonRoute
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openAdultDestination
import com.vitahealth.tata.app.navigation.openCaregiverTab
import com.vitahealth.tata.app.navigation.openFamilySummary
import com.vitahealth.tata.app.shell.FollowOmissionPush
import com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailRoute
import com.vitahealth.tata.monitoring.presentation.alerts.AlertsRoute
import com.vitahealth.tata.monitoring.presentation.notes.NotesRoute
import com.vitahealth.tata.monitoring.presentation.personalnotes.PersonalNotesRoute
import com.vitahealth.tata.monitoring.presentation.summary.FamilySummaryRoute
import com.vitahealth.tata.preferences.presentation.navigation.AccessibilityDestination
import com.vitahealth.tata.preferences.presentation.navigation.NotificationPreferencesDestination
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import kotlinx.coroutines.launch

/** Registers monitoringDestinations in the shared parent graph with unchanged route contracts. */
internal fun NavGraphBuilder.monitoringDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        RootDestination.LinkedPerson.route,
        arguments = listOf(
            navArgument("caregiverId") { type = NavType.StringType },
            navArgument("olderAdultId") { type = NavType.StringType },
            navArgument("olderAdultName") { type = NavType.StringType },
        ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val name = requireNotNull(entry.arguments?.getString("olderAdultName"))
        LinkedPersonRoute(
            factory = container.linkedPersonViewModelFactory(caregiver, adult, name),
            onBack = { navController.popBackStack() },
            onMedication = {
                navController.navigate(
                    RootDestination.MedicationManagement.createRoute(caregiver, adult, name)
                )
            },
            onHistory = { navController.navigate(AdherenceHistoryDestination.createRoute(adult)) },
            onTreatments = {
                navController.navigate(RootDestination.TreatmentList.createRoute(caregiver, adult, name))
            },
            onTabSelected = { tab ->
                if (tab == CaregiverTab.Person) {
                    navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiver))
                } else navController.openCaregiverTab(tab, caregiver, adult)
            },
        )
    }

    composable(
        RootDestination.FamilySummary.route,
        arguments =
            listOf(
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("olderAdultId") { type = NavType.StringType },
                navArgument("olderAdultName") { type = NavType.StringType },
            ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val name = requireNotNull(entry.arguments?.getString("olderAdultName"))
        FamilySummaryRoute(
            factory = container.familySummaryViewModelFactory(caregiver, adult, name),
            olderAdultName = name,
            onAgenda = { navController.navigate(RootDestination.IntakeAgenda.createRoute(adult)) },
            onHistory = { navController.navigate(AdherenceHistoryDestination.createRoute(adult)) },
            onAddMedication = {
                navController.navigate(
                    RootDestination.MedicationRegistration.createRoute(caregiver, adult, name)
                )
            },
            onChangePerson = {
                navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiver))
            },
            onAccessibility = {
                navController.navigate(AccessibilityDestination.createRoute(caregiver))
            },
            onNotificationPreferences = {
                navController.navigate(NotificationPreferencesDestination.createRoute(caregiver))
            },
            onTreatments = {
                navController.navigate(
                    RootDestination.TreatmentList.createRoute(caregiver, adult, name)
                )
            },
            onMedications = {
                navController.navigate(
                    RootDestination.MedicationManagement.createRoute(caregiver, adult, name)
                )
            },
            onAlerts = {
                navController.navigate(RootDestination.Alerts.createRoute(caregiver, adult))
            },
            onSubscription = {
                navController.navigate(RootDestination.PlanSubscription.createRoute(caregiver))
            },
            onNotes = {
                navController.navigate(RootDestination.Notes.createRoute(caregiver, adult))
            },
        )
        FollowOmissionPush(caregiver + adult) { container.followCaregiverAlerts(caregiver, adult) }
    }

    composable(
        RootDestination.Alerts.route,
        arguments =
            listOf(
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("olderAdultId") { type = NavType.StringType },
            ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        AlertsRoute(
            factory = container.alertsViewModelFactory(caregiver, adult),
            onOpenAlert = { alertId ->
                navController.navigate(
                    RootDestination.AlertDetail.createRoute(caregiver, adult, alertId)
                )
            },
            onOpenDoseSummary = { name -> navController.openFamilySummary(caregiver, adult, name) },
            onTabSelected = { tab ->
                if (tab != CaregiverTab.Alerts)
                    navController.openCaregiverTab(tab, caregiver, adult)
            },
        )
    }

    composable(
        RootDestination.AlertDetail.route,
        arguments =
            listOf(
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("olderAdultId") { type = NavType.StringType },
                navArgument("alertId") { type = NavType.LongType },
            ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val alertId = requireNotNull(entry.arguments?.getLong("alertId"))
        AlertDetailRoute(
            factory = container.alertDetailViewModelFactory(caregiver, adult, alertId),
            onBack = { navController.popBackStack() },
            onOpenProfile = { name -> navController.openFamilySummary(caregiver, adult, name) },
            onTabSelected = { tab ->
                if (tab == CaregiverTab.Alerts) navController.popBackStack()
                else navController.openCaregiverTab(tab, caregiver, adult)
            },
        )
    }

    composable(
        RootDestination.Notes.route,
        arguments =
            listOf(
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("olderAdultId") { type = NavType.StringType },
            ),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        NotesRoute(
            factory = container.notesViewModelFactory(caregiver, adult),
            onTabSelected = { tab ->
                if (tab != CaregiverTab.Notes) navController.openCaregiverTab(tab, caregiver, adult)
            },
        )
    }

    composable(
        RootDestination.PersonalNotes.route,
        arguments = listOf(navArgument("olderAdultId") { type = NavType.StringType }),
    ) { entry ->
        val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val scope = rememberCoroutineScope()
        PersonalNotesRoute(
            factory = container.personalNotesViewModelFactory(),
            onSignIn = {
                scope.launch {
                    container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
            onTab = { tab ->
                when (tab) {
                    AdultTab.Home ->
                        navController.popBackStack(RootDestination.NextDoseHome.route, false)
                    AdultTab.Medications ->
                        navController.openAdultDestination(
                            RootDestination.MyMedications.createRoute(adult)
                        )
                    AdultTab.Agenda ->
                        navController.openAdultDestination(
                            RootDestination.IntakeAgenda.createRoute(adult)
                        )
                    else -> Unit
                }
            },
        )
    }
}
