package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.BuildConfig
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.identity.infrastructure.external.rememberPublicContactAction
import com.vitahealth.tata.identity.presentation.access.PinAccessRoute
import com.vitahealth.tata.identity.presentation.access.SessionAccessRoute
import com.vitahealth.tata.identity.presentation.landing.LandingPlansRoute
import com.vitahealth.tata.identity.presentation.landing.LandingValueScreen
import com.vitahealth.tata.identity.presentation.onboarding.screens.OnboardingScreen
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationRoute

/**
 * Registers onboarding, access, PIN and caregiver registration without adding a nested back stack.
 */
internal fun NavGraphBuilder.identityDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(RootDestination.LandingValue.route) {
        LandingValueScreen(onNext = { navController.navigate(RootDestination.LandingPlans.route) })
    }
    composable(RootDestination.LandingPlans.route) {
        val contactAction = rememberPublicContactAction(BuildConfig.PUBLIC_CONTACT_URI)
        LandingPlansRoute(
            factory = container.landingPlansViewModelFactory,
            onBack = { navController.popBackStack() },
            onSelectPlan = { navController.navigate(RootDestination.Onboarding.route) },
            onContact = contactAction,
        )
    }
    composable(RootDestination.Onboarding.route) {
        OnboardingScreen(
            onStart = {
                container.completeOnboarding()
                // Sign-in goes underneath so going back from the registration lands on it.
                navController.navigate(RootDestination.SessionAccess.route) {
                    popUpTo(RootDestination.Onboarding.route) { inclusive = true }
                }
                navController.navigate(RootDestination.CaregiverRegistration.route)
            },
            onSignIn = {
                container.completeOnboarding()
                navController.navigate(RootDestination.SessionAccess.route) {
                    popUpTo(RootDestination.Onboarding.route) { inclusive = true }
                }
            },
        )
    }

    composable(RootDestination.SessionAccess.route) {
        val profile = container.rememberedAdultProfiles.current()
        val olderAdultName = profile?.name ?: "Adulto mayor"
        SessionAccessRoute(
            socialCredentials = container.socialCredentials,
            recovery = container.passwordRecovery,
            factory = container.sessionAccessViewModelFactory,
            onAuthenticated = { subject ->
                val route =
                    if (subject.role == "CAREGIVER") {
                        RootDestination.CaregiverProfiles.createRoute(subject.subjectId)
                    } else {
                        val name =
                            profile?.takeIf { it.id == subject.subjectId }?.name ?: "Adulto mayor"
                        RootDestination.NextDoseHome.createRoute(subject.subjectId, name)
                    }
                navController.navigate(route) {
                    popUpTo(RootDestination.SessionAccess.route) { inclusive = true }
                }
            },
            onRegister = { navController.navigate(RootDestination.CaregiverRegistration.route) },
            onPin = {
                profile?.let {
                    navController.navigate(
                        RootDestination.PinAccess.createRoute(it.id, olderAdultName, false)
                    )
                }
            },
            showPin = profile != null,
        )
    }
    composable(
        RootDestination.PinAccess.route,
        arguments =
            listOf(
                navArgument("olderAdultId") { type = NavType.StringType },
                navArgument("olderAdultName") { type = NavType.StringType },
                navArgument("setup") { type = NavType.BoolType },
            ),
    ) { entry ->
        val id = requireNotNull(entry.arguments?.getString("olderAdultId"))
        val name = requireNotNull(entry.arguments?.getString("olderAdultName"))
        PinAccessRoute(
            container.sessionAccessViewModelFactory,
            id,
            name,
            entry.arguments?.getBoolean("setup") ?: false,
            onComplete = {
                navController.navigate(RootDestination.NextDoseHome.createRoute(id, name)) {
                    popUpTo(RootDestination.PinAccess.route) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
        )
    }

    composable(RootDestination.CaregiverRegistration.route) {
        CaregiverRegistrationRoute(
            factory = container.caregiverRegistrationViewModelFactory,
            onRegistrationComplete = { caregiverId ->
                navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiverId)) {
                    popUpTo(RootDestination.CaregiverRegistration.route) { inclusive = true }
                }
            },
        )
    }
}
