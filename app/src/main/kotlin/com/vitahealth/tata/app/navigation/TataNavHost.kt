package com.vitahealth.tata.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vitahealth.tata.analytics.presentation.navigation.analyticsDestinations
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.graphs.careLinkDestinations
import com.vitahealth.tata.app.navigation.graphs.identityDestinations
import com.vitahealth.tata.app.navigation.graphs.intakeDestinations
import com.vitahealth.tata.app.navigation.graphs.inventoryDestinations
import com.vitahealth.tata.app.navigation.graphs.monitoringDestinations
import com.vitahealth.tata.app.navigation.graphs.preferencesDestinations
import com.vitahealth.tata.app.navigation.graphs.subscriptionDestinations
import com.vitahealth.tata.app.navigation.graphs.treatmentDestinations
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility

/**
 * Composes application destinations while preserving encoded arguments and role-specific back
 * stacks.
 */
@Composable
fun TataNavHost(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pushDestination: PushDestination? = null,
    onPushDestinationOpened: () -> Unit = {},
) {
    // Reduced motion removes the screen transitions; otherwise the Navigation default (700 ms fade)
    // applies.
    // A tapped omission notification opens its screen once the graph exists.
    LaunchedEffect(pushDestination) {
        if (pushDestination == null) return@LaunchedEffect
        if (!container.canOpenPushDestination(pushDestination)) {
            onPushDestinationOpened()
            return@LaunchedEffect
        }
        when (pushDestination) {
            is PushDestination.CaregiverAlerts -> {
                val (caregiver, adult) = pushDestination.caregiverId to pushDestination.olderAdultId
                navController.navigate(RootDestination.Alerts.createRoute(caregiver, adult))
                container.openAlertIdFor(pushDestination)?.let { alertId ->
                    if (!container.canOpenPushDestination(pushDestination)) return@let
                    navController.navigate(
                        RootDestination.AlertDetail.createRoute(caregiver, adult, alertId)
                    )
                }
            }
            is PushDestination.OlderAdultHome ->
                navController.navigate(
                    RootDestination.NextDoseHome.createRoute(
                        pushDestination.olderAdultId,
                        pushDestination.olderAdultName,
                    )
                )
            is PushDestination.OlderAdultDose ->
                navController.navigate(
                    RootDestination.DoseDetail.createRoute(pushDestination.intakeId)
                )
        }
        onPushDestinationOpened()
    }
    val reducedMotion = LocalTataAccessibility.current.reducedMotion
    // Read once: a changing start destination would reset the navigation graph.
    val startRoute = remember {
        if (container.hasSeenOnboarding()) RootDestination.SessionAccess.route
        else RootDestination.LandingValue.route
    }
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
        enterTransition = {
            if (reducedMotion) EnterTransition.None else fadeIn(animationSpec = tween(700))
        },
        exitTransition = {
            if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(700))
        },
        popEnterTransition = {
            if (reducedMotion) EnterTransition.None else fadeIn(animationSpec = tween(700))
        },
        popExitTransition = {
            if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(700))
        },
    ) {
        identityDestinations(navController, container)
        treatmentDestinations(navController, container)
        careLinkDestinations(navController, container)
        monitoringDestinations(navController, container)
        intakeDestinations(navController, container)
        preferencesDestinations(
            navController,
            container::accessibilityViewModelFactory,
            container::notificationPreferencesViewModelFactory,
        )
        analyticsDestinations(
            navController,
            container.analytics::adherenceHistoryViewModelFactory,
            container.analytics::adherenceRecommendationsViewModelFactory,
            onTabSelected = { navController.openCaregiverTab(it) },
            onApplyAdjustments = { adultId ->
                val family =
                    runCatching {
                            navController.getBackStackEntry(RootDestination.FamilySummary.route)
                        }
                        .getOrNull()
                val caregiver = family?.arguments?.getString("caregiverId")
                val adult = family?.arguments?.getString("olderAdultId")
                val name = family?.arguments?.getString("olderAdultName")
                if (caregiver != null && adult == adultId && name != null) {
                    navController.navigate(
                        RootDestination.TreatmentList.createRoute(caregiver, adultId, name)
                    )
                } else {
                    navController.popBackStack()
                }
            },
        )
        inventoryDestinations(navController, container::inventoryViewModelFactory)
        subscriptionDestinations(navController, container)
    }
}

/** Adult bottom tabs preserve their state without growing a stack for every tab switch. */
internal fun NavHostController.openAdultDestination(route: String) {
    navigate(route) {
        popUpTo(RootDestination.NextDoseHome.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
