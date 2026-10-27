package com.vitahealth.tata.app.navigation.graphs

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.openAdultDestination
import com.vitahealth.tata.app.shell.FollowOmissionPush
import com.vitahealth.tata.carelink.presentation.consent.ConsentWithdrawalAction
import com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaRoute
import com.vitahealth.tata.intake.presentation.detail.DoseDetailRoute
import com.vitahealth.tata.intake.presentation.home.NextDoseHomeRoute
import com.vitahealth.tata.intake.presentation.voice.VoiceConfirmationRoute
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import kotlinx.coroutines.launch

/** Registers intakeDestinations in the shared parent graph with unchanged route contracts. */
internal fun NavGraphBuilder.intakeDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        route = RootDestination.NextDoseHome.route,
        arguments =
            listOf(
                navArgument(RootDestination.NextDoseHome.olderAdultIdArgument) {
                    type = NavType.StringType
                },
                navArgument(RootDestination.NextDoseHome.olderAdultNameArgument) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.NextDoseHome.olderAdultIdArgument
                )
            )
        val olderAdultName =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.NextDoseHome.olderAdultNameArgument
                )
            )

        val scope = rememberCoroutineScope()
        NextDoseHomeRoute(
            factory =
                container.intake.nextDoseHomeViewModelFactory(
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
            accountActions = {
                ConsentWithdrawalAction(container.consentWithdrawalFactory(olderAdultId)) {
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
            onOpenVoice = {
                navController.navigate(RootDestination.VoiceConfirmation.createRoute(it))
            },
            onOpenDoseDetail = { intakeId ->
                navController.navigate(RootDestination.DoseDetail.createRoute(intakeId))
            },
            onSignOut = {
                scope.launch {
                    container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
            onOpenMedications = {
                navController.openAdultDestination(
                    RootDestination.MyMedications.createRoute(olderAdultId)
                )
            },
            onOpenNotes = {
                navController.openAdultDestination(
                    RootDestination.PersonalNotes.createRoute(olderAdultId)
                )
            },
            onOpenAgenda = {
                navController.openAdultDestination(
                    RootDestination.IntakeAgenda.createRoute(olderAdultId)
                )
            },
        )
        FollowOmissionPush(olderAdultId) {
            container.followDoseReminders(olderAdultId, olderAdultName)
        }
    }

    composable(
        route = RootDestination.IntakeAgenda.route,
        arguments =
            listOf(
                navArgument(RootDestination.IntakeAgenda.olderAdultIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { backStackEntry ->
        val olderAdultId =
            requireNotNull(
                backStackEntry.arguments?.getString(
                    RootDestination.IntakeAgenda.olderAdultIdArgument
                )
            )
        IntakeAgendaRoute(
            factory = container.intake.intakeAgendaViewModelFactory(olderAdultId),
            onOpenDose = { navController.navigate(RootDestination.DoseDetail.createRoute(it)) },
            onMedications = {
                navController.openAdultDestination(
                    RootDestination.MyMedications.createRoute(olderAdultId)
                )
            },
            onNotes = {
                navController.openAdultDestination(
                    RootDestination.PersonalNotes.createRoute(olderAdultId)
                )
            },
            onHome = { navController.popBackStack(RootDestination.NextDoseHome.route, false) },
        )
    }

    composable(
        RootDestination.VoiceConfirmation.route,
        arguments = listOf(navArgument("intakeId") { type = NavType.StringType }),
    ) { entry ->
        val id = requireNotNull(entry.arguments?.getString("intakeId"))
        VoiceConfirmationRoute(
            factory = container.intake.voiceConfirmationViewModelFactory(id),
            onBack = { navController.popBackStack() },
            onHome = {
                if (!navController.popBackStack(RootDestination.NextDoseHome.route, false))
                    navController.popBackStack()
            },
            onAdultTab = { tab, adult ->
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
                    AdultTab.Notes ->
                        navController.openAdultDestination(
                            RootDestination.PersonalNotes.createRoute(adult)
                        )
                    else -> Unit
                }
            },
        )
    }

    composable(
        route = RootDestination.DoseDetail.route,
        arguments =
            listOf(
                navArgument(RootDestination.DoseDetail.intakeIdArgument) {
                    type = NavType.StringType
                }
            ),
    ) { backStackEntry ->
        val intakeId =
            requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.DoseDetail.intakeIdArgument)
            )

        DoseDetailRoute(
            factory = container.intake.doseDetailViewModelFactory(intakeId),
            onAdultTab = { tab, adult ->
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
                    AdultTab.Notes ->
                        navController.openAdultDestination(
                            RootDestination.PersonalNotes.createRoute(adult)
                        )
                    else -> Unit
                }
            },
            onBack = { navController.popBackStack() },
            onHome = {
                if (!navController.popBackStack(RootDestination.NextDoseHome.route, false))
                    navController.popBackStack()
            },
        )
    }
}
