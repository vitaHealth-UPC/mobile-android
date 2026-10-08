package com.vitahealth.tata.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitahealth.tata.analytics.presentation.history.AdherenceHistoryRoute
import com.vitahealth.tata.analytics.presentation.recommendations.AdherenceRecommendationsRoute
import com.vitahealth.tata.app.TataApplication
import com.vitahealth.tata.app.shell.FollowOmissionPush
import com.vitahealth.tata.omission.application.queries.PushDestination
import com.vitahealth.tata.carelink.presentation.link.CareLinkRoute
import com.vitahealth.tata.identity.presentation.subscription.PlanSubscriptionRoute
import com.vitahealth.tata.identity.presentation.registration.CaregiverRegistrationRoute
import com.vitahealth.tata.intake.presentation.detail.DoseDetailRoute
import com.vitahealth.tata.intake.presentation.home.NextDoseHomeRoute
import com.vitahealth.tata.inventory.presentation.inventory.InventoryRoute
import com.vitahealth.tata.preferences.presentation.accessibility.AccessibilityRoute
import com.vitahealth.tata.preferences.presentation.notifications.NotificationPreferencesRoute
import com.vitahealth.tata.shared.design.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.design.components.CaregiverTab
import com.vitahealth.tata.treatment.presentation.medication.MedicationManagementRoute
import com.vitahealth.tata.treatment.presentation.medication.MedicationRegistrationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentCreationRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDoseFrequencyRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentScheduleInstructionsRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentReminderRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailRoute
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun TataNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    pushDestination: PushDestination? = null,
    onPushDestinationOpened: () -> Unit = {},
) {
    // Reduced motion removes the screen transitions; otherwise the Navigation default (700 ms fade) applies.
    // A tapped omission notification opens its screen once the graph exists.
    val pushApp = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
    androidx.compose.runtime.LaunchedEffect(pushDestination) {
        when (pushDestination) {
            is PushDestination.CaregiverAlerts -> {
                val (caregiver, adult) = pushDestination.caregiverId to pushDestination.olderAdultId
                navController.navigate(RootDestination.Alerts.createRoute(caregiver, adult))
                pushApp.container.openAlertIdFor(pushDestination)?.let { alertId ->
                    navController.navigate(RootDestination.AlertDetail.createRoute(caregiver, adult, alertId))
                }
            }
            is PushDestination.OlderAdultHome ->
                navController.navigate(RootDestination.NextDoseHome.createRoute(pushDestination.olderAdultId, pushDestination.olderAdultName))
            null -> return@LaunchedEffect
        }
        onPushDestinationOpened()
    }
    val reducedMotion = LocalTataAccessibility.current.reducedMotion
    val container = (androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication).container
    // Read once: a changing start destination would reset the navigation graph.
    val startRoute = androidx.compose.runtime.remember {
        if (container.hasSeenOnboarding()) RootDestination.SessionAccess.route else RootDestination.Onboarding.route
    }
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
        enterTransition = { if (reducedMotion) EnterTransition.None else fadeIn(animationSpec = tween(700)) },
        exitTransition = { if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(700)) },
        popEnterTransition = { if (reducedMotion) EnterTransition.None else fadeIn(animationSpec = tween(700)) },
        popExitTransition = { if (reducedMotion) ExitTransition.None else fadeOut(animationSpec = tween(700)) },
    ) {
        composable(RootDestination.Onboarding.route) {
            com.vitahealth.tata.identity.presentation.onboarding.OnboardingScreen(
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
            val context = androidx.compose.ui.platform.LocalContext.current
            val app = context.applicationContext as TataApplication
            val deviceProfile = context.getSharedPreferences("tata_adult_profile",android.content.Context.MODE_PRIVATE)
            com.vitahealth.tata.identity.presentation.access.SessionAccessRoute(
                factory=app.container.sessionAccessViewModelFactory,
                onAuthenticated={ subject ->
                    val route=if(subject.role=="CAREGIVER") RootDestination.CaregiverProfiles.createRoute(subject.subjectId)
                    else RootDestination.NextDoseHome.createRoute(subject.subjectId,deviceProfile.getString("name","Adulto mayor") ?: "Adulto mayor")
                    navController.navigate(route) { popUpTo(RootDestination.SessionAccess.route) { inclusive=true } }
                },
                onRegister={navController.navigate(RootDestination.CaregiverRegistration.route)},
                onPin={navController.navigate(RootDestination.PinAccess.createRoute(requireNotNull(deviceProfile.getString("id",null)),deviceProfile.getString("name","Adulto mayor") ?: "Adulto mayor",false))},
                showPin=deviceProfile.contains("id"),
            )
        }
        composable(RootDestination.PinAccess.route,arguments=listOf(navArgument("olderAdultId"){type=NavType.StringType},navArgument("olderAdultName"){type=NavType.StringType},navArgument("setup"){type=NavType.BoolType})) { entry ->
            val app=androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val id=requireNotNull(entry.arguments?.getString("olderAdultId"))
            val name=requireNotNull(entry.arguments?.getString("olderAdultName"))
            com.vitahealth.tata.identity.presentation.access.PinAccessRoute(app.container.sessionAccessViewModelFactory,id,name,entry.arguments?.getBoolean("setup") ?: false,
                onComplete={navController.navigate(RootDestination.NextDoseHome.createRoute(id,name)){popUpTo(RootDestination.PinAccess.route){inclusive=true}}},
                onBack={navController.popBackStack()})
        }

        composable(RootDestination.TreatmentList.route,arguments=listOf(navArgument("caregiverId"){type=NavType.StringType},navArgument("olderAdultId"){type=NavType.StringType},navArgument("olderAdultName"){type=NavType.StringType})) { entry ->
            val app=androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver=requireNotNull(entry.arguments?.getString("caregiverId"));val adult=requireNotNull(entry.arguments?.getString("olderAdultId"));val name=requireNotNull(entry.arguments?.getString("olderAdultName"))
            com.vitahealth.tata.treatment.presentation.treatment.TreatmentListRoute(app.container.treatmentListViewModelFactory(caregiver,adult),name,
                onOpen={id->navController.navigate(RootDestination.TreatmentDetail.createRoute(caregiver,name,id,""))},
                onAddMedication={navController.navigate(RootDestination.MedicationRegistration.createRoute(caregiver,adult,name))},
                onBack={navController.popBackStack()})
        }

        composable(RootDestination.CaregiverProfiles.route,arguments=listOf(navArgument("caregiverId"){type=NavType.StringType})) { entry ->
            val app=androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver=requireNotNull(entry.arguments?.getString("caregiverId"))
            val scope=androidx.compose.runtime.rememberCoroutineScope()
            com.vitahealth.tata.carelink.presentation.profiles.CaregiverProfilesRoute(
                factory=app.container.caregiverProfilesViewModelFactory(caregiver),
                onOpenAdult={adult->navController.navigate(RootDestination.FamilySummary.createRoute(caregiver,adult.id,adult.name))},
                onAdultConsent={link->navController.navigate(RootDestination.CareLink.createRoute(caregiver,link.code,link.olderAdultId))},
                onSignOut={scope.launch{app.container.signOut();navController.navigate(RootDestination.SessionAccess.route){popUpTo(navController.graph.id){inclusive=true}}}},
            )
        }

        composable(RootDestination.CaregiverRegistration.route) {
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            CaregiverRegistrationRoute(
                factory = app.container.caregiverRegistrationViewModelFactory,
                onRegistrationComplete = { caregiverId ->
                    navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiverId)) {
                        popUpTo(RootDestination.CaregiverRegistration.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(
            route = RootDestination.CareLink.route,
            arguments = listOf(
                navArgument(RootDestination.CareLink.caregiverIdArgument) { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType; defaultValue = "" },
                navArgument("previewAdult") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.CareLink.caregiverIdArgument),
            )
            CareLinkRoute(
                factory = app.container.careLinkViewModelFactory(caregiverId),
                initialCode = backStackEntry.arguments?.getString("code") ?: "",
                initialOlderAdultId = backStackEntry.arguments?.getString("previewAdult") ?: "",
                onConfirmed = { olderAdultId, olderAdultName ->
                    app.getSharedPreferences("tata_adult_profile",android.content.Context.MODE_PRIVATE).edit().putString("id",olderAdultId).putString("name",olderAdultName).apply()
                    navController.navigate(RootDestination.PinAccess.createRoute(olderAdultId,olderAdultName,true)) {
                        popUpTo(RootDestination.CareLink.route) { inclusive = true }
                    }
                },
            )
        }

        composable(RootDestination.FamilySummary.route, arguments = listOf(
            navArgument("caregiverId") { type = NavType.StringType },
            navArgument("olderAdultId") { type = NavType.StringType },
            navArgument("olderAdultName") { type = NavType.StringType },
        )) { entry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
            val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
            val name = requireNotNull(entry.arguments?.getString("olderAdultName"))
            com.vitahealth.tata.monitoring.presentation.summary.FamilySummaryRoute(
                factory = app.container.familySummaryViewModelFactory(caregiver, adult, name),
                olderAdultName = name,
                onAgenda = { navController.navigate(RootDestination.IntakeAgenda.createRoute(adult)) },
                onHistory = { navController.navigate(RootDestination.AdherenceHistory.createRoute(adult)) },
                onAddMedication = { navController.navigate(RootDestination.MedicationRegistration.createRoute(caregiver, adult, name)) },
                onChangePerson = { navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiver)) },
                onAccessibility = { navController.navigate(RootDestination.Accessibility.createRoute(caregiver)) },
                onNotificationPreferences = { navController.navigate(RootDestination.NotificationPreferences.createRoute(caregiver)) },
                onTreatments = { navController.navigate(RootDestination.TreatmentList.createRoute(caregiver,adult,name)) },
                onMedications = { navController.navigate(RootDestination.MedicationManagement.createRoute(caregiver, adult, name)) },
                onAlerts = { navController.navigate(RootDestination.Alerts.createRoute(caregiver, adult)) },
                onSubscription = { navController.navigate(RootDestination.PlanSubscription.createRoute(caregiver)) },
                onNotes = { navController.navigate(RootDestination.Notes.createRoute(caregiver, adult)) },
            )
            FollowOmissionPush(caregiver + adult) { app.container.followCaregiverAlerts(caregiver, adult) }
        }

        composable(
            route = RootDestination.PlanSubscription.route,
            arguments = listOf(
                navArgument(RootDestination.PlanSubscription.accountIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val accountId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.PlanSubscription.accountIdArgument),
            )
            PlanSubscriptionRoute(
                factory = app.container.planSubscriptionViewModelFactory(accountId),
                onBack = { navController.popBackStack() },
                onTabSelected = { tab ->
                    if (tab != CaregiverTab.More) {
                        val family = runCatching { navController.getBackStackEntry(RootDestination.FamilySummary.route) }.getOrNull()
                        val caregiver = family?.arguments?.getString("caregiverId")
                        val adult = family?.arguments?.getString("olderAdultId")
                        if (caregiver != null && adult != null) {
                            if (tab == CaregiverTab.Person) navController.navigate(RootDestination.CaregiverProfiles.createRoute(caregiver))
                            else navController.openCaregiverTab(tab, caregiver, adult)
                        } else navController.popBackStack()
                    }
                },
            )
        }

        composable(RootDestination.Alerts.route, arguments = listOf(
            navArgument("caregiverId") { type = NavType.StringType },
            navArgument("olderAdultId") { type = NavType.StringType },
        )) { entry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
            val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
            com.vitahealth.tata.monitoring.presentation.alerts.AlertsRoute(
                factory = app.container.alertsViewModelFactory(caregiver, adult),
                onOpenAlert = { alertId -> navController.navigate(RootDestination.AlertDetail.createRoute(caregiver, adult, alertId)) },
                onTabSelected = { tab -> if (tab != CaregiverTab.Alerts) navController.openCaregiverTab(tab, caregiver, adult) },
            )
        }

        composable(RootDestination.AlertDetail.route, arguments = listOf(
            navArgument("caregiverId") { type = NavType.StringType },
            navArgument("olderAdultId") { type = NavType.StringType },
            navArgument("alertId") { type = NavType.LongType },
        )) { entry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
            val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
            val alertId = requireNotNull(entry.arguments?.getLong("alertId"))
            com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailRoute(
                factory = app.container.alertDetailViewModelFactory(caregiver, adult, alertId),
                onBack = { navController.popBackStack() },
                onTabSelected = { tab ->
                    if (tab == CaregiverTab.Alerts) navController.popBackStack()
                    else navController.openCaregiverTab(tab, caregiver, adult)
                },
            )
        }

        composable(RootDestination.Notes.route, arguments = listOf(
            navArgument("caregiverId") { type = NavType.StringType },
            navArgument("olderAdultId") { type = NavType.StringType },
        )) { entry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiver = requireNotNull(entry.arguments?.getString("caregiverId"))
            val adult = requireNotNull(entry.arguments?.getString("olderAdultId"))
            com.vitahealth.tata.monitoring.presentation.notes.NotesRoute(
                factory = app.container.notesViewModelFactory(caregiver, adult),
                onTabSelected = { tab -> if (tab != CaregiverTab.Notes) navController.openCaregiverTab(tab, caregiver, adult) },
            )
        }

        composable(
            route = RootDestination.NotificationPreferences.route,
            arguments = listOf(
                navArgument(RootDestination.NotificationPreferences.userIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val userId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.NotificationPreferences.userIdArgument),
            )
            NotificationPreferencesRoute(
                factory = app.container.notificationPreferencesViewModelFactory(userId),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = RootDestination.Accessibility.route,
            arguments = listOf(
                navArgument(RootDestination.Accessibility.userIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val userId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.Accessibility.userIdArgument),
            )
            AccessibilityRoute(
                factory = app.container.accessibilityViewModelFactory(userId),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = RootDestination.MedicationManagement.route,
            arguments = listOf(
                navArgument(RootDestination.MedicationManagement.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationManagement.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationManagement.olderAdultNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationManagement.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationManagement.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationManagement.olderAdultNameArgument),
            )
            MedicationManagementRoute(
                factory = app.container.medicationManagementViewModelFactory(caregiverId, olderAdultId, olderAdultName),
                onBack = { navController.popBackStack() },
                onAddMedication = {
                    navController.navigate(
                        RootDestination.MedicationRegistration.createRoute(caregiverId, olderAdultId, olderAdultName),
                    )
                },
            )
        }

        composable(
            route = RootDestination.MedicationRegistration.route,
            arguments = listOf(
                navArgument(RootDestination.MedicationRegistration.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationRegistration.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.MedicationRegistration.olderAdultNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.MedicationRegistration.olderAdultNameArgument),
            )

            MedicationRegistrationRoute(
                onBack = { navController.popBackStack() },
                factory = app.container.medicationRegistrationViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
                onRegistered = { medication, draft ->
                    navController.navigate(
                        RootDestination.TreatmentCreation.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medication.id,
                            medicationLabel = medication.name + " · " + medication.presentation,
                        ),
                    ) {
                        popUpTo(RootDestination.MedicationRegistration.route) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("draftFrequency", draft.frequency)
                        set("draftInstructions", listOf(draft.timing, draft.notes).filter { it.isNotBlank() }.joinToString("\n"))
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentCreation.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentCreation.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentCreation.medicationLabelArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentCreation.medicationLabelArgument),
            )

            TreatmentCreationRoute(
                factory = app.container.treatmentCreationViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                ),
                onCreated = { treatment ->
                    navController.navigate(
                        RootDestination.TreatmentDoseFrequency.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medicationId,
                            medicationLabel = medicationLabel,
                            treatmentId = treatment.id,
                            treatmentName = treatment.name,
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentCreation.route) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("draftFrequency", backStackEntry.savedStateHandle.get<String>("draftFrequency") ?: "")
                        set("draftInstructions", backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "")
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentDoseFrequency.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentDoseFrequency.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDoseFrequency.treatmentNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDoseFrequency.treatmentNameArgument),
            )

            TreatmentDoseFrequencyRoute(
                initialFrequency = backStackEntry.savedStateHandle.get<String>("draftFrequency") ?: "",
                factory = app.container.treatmentDoseFrequencyViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                ),
                onCompleted = { basics ->
                    navController.navigate(
                        RootDestination.TreatmentScheduleInstructions.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medicationId,
                            medicationLabel = medicationLabel,
                            treatmentId = treatmentId,
                            treatmentName = treatmentName,
                            dosage = basics.dosage.value,
                            frequency = basics.frequency.value,
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentDoseFrequency.route) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set("draftInstructions",
                        backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "")
                },
            )
        }

        composable(
            route = RootDestination.TreatmentScheduleInstructions.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentScheduleInstructions.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentScheduleInstructions.frequencyArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentScheduleInstructions.frequencyArgument),
            )

            TreatmentScheduleInstructionsRoute(
                initialInstructions = backStackEntry.savedStateHandle.get<String>("draftInstructions") ?: "",
                factory = app.container.treatmentScheduleInstructionsViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    dosage = dosage,
                    frequency = frequency,
                ),
                onCompleted = { schedule ->
                    val scheduleText = schedule.schedule.times.joinToString(",") {
                        it.format(DateTimeFormatter.ofPattern("HH:mm"))
                    }
                    navController.navigate(
                        RootDestination.TreatmentReminders.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medicationId,
                            medicationLabel = medicationLabel,
                            treatmentId = treatmentId,
                            treatmentName = treatmentName,
                            dosage = dosage,
                            frequency = frequency,
                            scheduleText = scheduleText,
                            instructions = schedule.instructions.value,
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentScheduleInstructions.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentReminders.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentReminders.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.frequencyArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.scheduleTextArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentReminders.instructionsArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.frequencyArgument),
            )
            val scheduleText = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.scheduleTextArgument),
            )
            val instructions = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentReminders.instructionsArgument),
            ).trim()

            TreatmentReminderRoute(
                factory = app.container.treatmentReminderViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    dosage = dosage,
                    frequency = frequency,
                    scheduleText = scheduleText,
                    instructions = instructions,
                ),
                onCompleted = { policy ->
                    navController.navigate(
                        RootDestination.TreatmentLifecycle.createRoute(
                            caregiverId = caregiverId,
                            olderAdultId = olderAdultId,
                            olderAdultName = olderAdultName,
                            medicationId = medicationId,
                            medicationLabel = medicationLabel,
                            treatmentId = treatmentId,
                            treatmentName = treatmentName,
                            dosage = dosage,
                            frequency = frequency,
                            scheduleText = scheduleText,
                            instructions = instructions,
                            reminderDelay = policy.followUpDelayMinutes.value,
                        ),
                    ) {
                        popUpTo(RootDestination.TreatmentReminders.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = RootDestination.TreatmentLifecycle.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentLifecycle.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.medicationLabelArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.treatmentNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.dosageArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.frequencyArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.scheduleTextArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.instructionsArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentLifecycle.reminderDelayArgument) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.caregiverIdArgument),
            )
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.olderAdultNameArgument),
            )
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.medicationIdArgument),
            )
            val medicationLabel = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.medicationLabelArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.treatmentIdArgument),
            )
            val treatmentName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.treatmentNameArgument),
            )
            val dosage = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.dosageArgument),
            )
            val frequency = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.frequencyArgument),
            )
            val scheduleText = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.scheduleTextArgument),
            )
            val instructions = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentLifecycle.instructionsArgument),
            ).trim()
            val reminderDelay = requireNotNull(
                backStackEntry.arguments?.getInt(RootDestination.TreatmentLifecycle.reminderDelayArgument),
            )

            TreatmentLifecycleRoute(
                factory = app.container.treatmentLifecycleViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                    medicationId = medicationId,
                    medicationLabel = medicationLabel,
                    treatmentId = treatmentId,
                    treatmentName = treatmentName,
                    dosage = dosage,
                    frequency = frequency,
                    scheduleText = scheduleText,
                    instructions = instructions,
                    reminderDelayMinutes = reminderDelay,
                ),
                onOpenDetail = {
                    navController.navigate(
                        RootDestination.TreatmentDetail.createRoute(
                            caregiverId = caregiverId,
                            olderAdultName = olderAdultName,
                            treatmentId = treatmentId,
                            medicationLabelHint = medicationLabel,
                        ),
                    )
                },
            )
        }

        composable(
            route = RootDestination.TreatmentDetail.route,
            arguments = listOf(
                navArgument(RootDestination.TreatmentDetail.caregiverIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.olderAdultNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.treatmentIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.TreatmentDetail.medicationLabelHintArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val caregiverId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.caregiverIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.olderAdultNameArgument),
            )
            val treatmentId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.treatmentIdArgument),
            )
            val medicationLabelHint = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.TreatmentDetail.medicationLabelHintArgument),
            ).trim()

            TreatmentDetailRoute(
                factory = app.container.treatmentDetailViewModelFactory(
                    caregiverId = caregiverId,
                    olderAdultName = olderAdultName,
                    treatmentId = treatmentId,
                    medicationLabelHint = medicationLabelHint,
                ),
                onBack = { navController.popBackStack() },
                onOpenInventory = { medicationId, medicationName ->
                    navController.navigate(
                        RootDestination.Inventory.createRoute(
                            medicationId = medicationId,
                            medicationName = medicationName,
                            unit = "",
                        ),
                    )
                },
            )
        }
        composable(
            route = RootDestination.NextDoseHome.route,
            arguments = listOf(
                navArgument(RootDestination.NextDoseHome.olderAdultIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.NextDoseHome.olderAdultNameArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.NextDoseHome.olderAdultIdArgument),
            )
            val olderAdultName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.NextDoseHome.olderAdultNameArgument),
            )

            val scope=androidx.compose.runtime.rememberCoroutineScope()
            NextDoseHomeRoute(
                factory = app.container.nextDoseHomeViewModelFactory(
                    olderAdultId = olderAdultId,
                    olderAdultName = olderAdultName,
                ),
                onOpenDoseDetail = { intakeId ->
                    navController.navigate(RootDestination.DoseDetail.createRoute(intakeId))
                },
                onSignOut = {scope.launch{app.container.signOut();navController.navigate(RootDestination.SessionAccess.route){popUpTo(navController.graph.id){inclusive=true}}}},
                onOpenMedications = { navController.navigate(RootDestination.MyMedications.createRoute(olderAdultId)) },
                onOpenAgenda = { navController.navigate(RootDestination.IntakeAgenda.createRoute(olderAdultId)) },
            )
            FollowOmissionPush(olderAdultId) { app.container.followDoseReminders(olderAdultId, olderAdultName) }
        }

        composable(
            route = RootDestination.MyMedications.route,
            arguments = listOf(navArgument("olderAdultId") { type = NavType.StringType }),
        ) { entry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(entry.arguments?.getString("olderAdultId"))
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            com.vitahealth.tata.treatment.presentation.medication.MyMedicationsRoute(
                factory = app.container.myMedicationsViewModelFactory(olderAdultId),
                onSignIn = { scope.launch {
                    app.container.signOut()
                    navController.navigate(RootDestination.SessionAccess.route) { popUpTo(navController.graph.id) { inclusive = true } }
                } },
                onHome = { navController.popBackStack(RootDestination.NextDoseHome.route, false) },
                onAgenda = { navController.navigate(RootDestination.IntakeAgenda.createRoute(olderAdultId)) },
                onOpenDose = { navController.navigate(RootDestination.DoseDetail.createRoute(it)) },
            )
        }

        composable(
            route = RootDestination.IntakeAgenda.route,
            arguments = listOf(navArgument(RootDestination.IntakeAgenda.olderAdultIdArgument) { type = NavType.StringType }),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(backStackEntry.arguments?.getString(RootDestination.IntakeAgenda.olderAdultIdArgument))
            com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaRoute(
                factory = app.container.intakeAgendaViewModelFactory(olderAdultId),
                onOpenDose = { navController.navigate(RootDestination.DoseDetail.createRoute(it)) },
                onHome = { navController.popBackStack() },
            )
        }

        composable(
            route = RootDestination.DoseDetail.route,
            arguments = listOf(
                navArgument(RootDestination.DoseDetail.intakeIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val intakeId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.DoseDetail.intakeIdArgument),
            )

            DoseDetailRoute(
                factory = app.container.doseDetailViewModelFactory(intakeId),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = RootDestination.Inventory.route,
            arguments = listOf(
                navArgument(RootDestination.Inventory.medicationIdArgument) { type = NavType.StringType },
                navArgument(RootDestination.Inventory.medicationNameArgument) { type = NavType.StringType },
                navArgument(RootDestination.Inventory.unitArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val medicationId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.Inventory.medicationIdArgument),
            )
            val medicationName = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.Inventory.medicationNameArgument),
            ).trim()
            val unit = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.Inventory.unitArgument),
            ).trim()

            InventoryRoute(
                olderAdultName = runCatching { navController.getBackStackEntry(RootDestination.FamilySummary.route) }
                    .getOrNull()?.arguments?.getString("olderAdultName") ?: "",
                onTabSelected = { tab -> navController.openCaregiverTab(tab) },
                factory = app.container.inventoryViewModelFactory(
                    medicationId = medicationId,
                    medicationName = medicationName,
                    unit = unit,
                ),
            )
        }

        composable(
            route = RootDestination.AdherenceHistory.route,
            arguments = listOf(
                navArgument(RootDestination.AdherenceHistory.olderAdultIdArgument) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.AdherenceHistory.olderAdultIdArgument),
            )

            AdherenceHistoryRoute(
                factory = app.container.adherenceHistoryViewModelFactory(olderAdultId),
                onOpenRecommendations = {
                    navController.navigate(RootDestination.AdherenceRecommendations.createRoute(olderAdultId))
                },
                onTabSelected = { tab -> navController.openCaregiverTab(tab) },
            )
        }

        composable(
            route = RootDestination.AdherenceRecommendations.route,
            arguments = listOf(
                navArgument(RootDestination.AdherenceRecommendations.olderAdultIdArgument) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TataApplication
            val olderAdultId = requireNotNull(
                backStackEntry.arguments?.getString(RootDestination.AdherenceRecommendations.olderAdultIdArgument),
            )

            AdherenceRecommendationsRoute(
                factory = app.container.adherenceRecommendationsViewModelFactory(olderAdultId),
                onBackToHistory = { navController.popBackStack() },
                onTabSelected = { tab -> navController.openCaregiverTab(tab) },
            )
        }

    }
}
