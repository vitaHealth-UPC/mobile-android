package com.vitahealth.tata.app.navigation.graphs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vitahealth.tata.app.di.AppContainer
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.carelink.presentation.link.CareLinkRoute
import com.vitahealth.tata.carelink.presentation.profiles.CaregiverProfilesRoute
import com.vitahealth.tata.identity.presentation.access.SocialIdentityLinkDialog
import kotlinx.coroutines.launch

/** Registers careLinkDestinations in the shared parent graph with unchanged route contracts. */
internal fun NavGraphBuilder.careLinkDestinations(
    navController: NavHostController,
    container: AppContainer,
) {
    composable(
        RootDestination.CaregiverProfiles.route,
        arguments = listOf(navArgument("caregiverId") { type = NavType.StringType }),
    ) { entry ->
        val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
        val scope = rememberCoroutineScope()
        var showSocialLink by remember { mutableStateOf(false) }
        CaregiverProfilesRoute(
            factory = container.caregiverProfilesViewModelFactory(caregiver),
            onOpenAdult = { adult ->
                navController.navigate(
                    RootDestination.LinkedPerson.createRoute(caregiver, adult.id, adult.name)
                )
            },
            onAdultConsent = { link ->
                navController.navigate(
                    RootDestination.CareLink.createRoute(caregiver, link.code, link.olderAdultId)
                )
            },
            onLinkSocial = { showSocialLink = true },
            onSignOut = {
                scope.launch {
                    container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
        )
        if (showSocialLink) {
            SocialIdentityLinkDialog(
                credentials = container.socialCredentials,
                link = { firebaseIdToken -> container.linkSocialIdentity(firebaseIdToken) },
                onDismiss = { showSocialLink = false },
            )
        }
    }

    composable(
        route = RootDestination.CareLink.route,
        arguments =
            listOf(
                navArgument(RootDestination.CareLink.caregiverIdArgument) {
                    type = NavType.StringType
                },
                navArgument("code") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("previewAdult") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
    ) { backStackEntry ->
        val caregiverId =
            requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.CareLink.caregiverIdArgument)
            )
        CareLinkRoute(
            factory = container.careLinkViewModelFactory(caregiverId),
            initialCode = backStackEntry.arguments?.getString("code") ?: "",
            initialOlderAdultId = backStackEntry.arguments?.getString("previewAdult") ?: "",
            onConfirmed = { olderAdultId, olderAdultName ->
                container.rememberedAdultProfiles.remember(olderAdultId, olderAdultName)
                navController.navigate(
                    RootDestination.PinAccess.createRoute(olderAdultId, olderAdultName, true)
                ) {
                    popUpTo(RootDestination.CareLink.route) { inclusive = true }
                }
            },
        )
    }
}
