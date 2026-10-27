package com.vitahealth.tata.app.navigation.graphs

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.vitahealth.tata.app.navigation.destinations.RootDestination

/** Shares editable fields only within this owner, adult, medication and treatment draft. */
internal fun treatmentWizardDraftState(
    controller: NavHostController,
    current: NavBackStackEntry,
): SavedStateHandle {
    val root =
        runCatching { controller.getBackStackEntry(RootDestination.TreatmentDoseFrequency.route) }
            .getOrNull()
    val identity = listOf("caregiverId", "olderAdultId", "medicationId", "treatmentId")
    val sameDraft =
        root != null &&
            identity.all { key ->
                val value = current.arguments?.getString(key)
                value != null && value == root.arguments?.getString(key)
            }
    return (if (sameDraft) requireNotNull(root) else current).savedStateHandle
}

/** Reopens only this draft; a direct review entry seeds a new wizard with its existing inputs. */
internal fun NavHostController.completeTreatmentConfiguration(current: NavBackStackEntry) {
    val root =
        runCatching { getBackStackEntry(RootDestination.TreatmentDoseFrequency.route) }.getOrNull()
    val keys = listOf("caregiverId", "olderAdultId", "medicationId", "treatmentId")
    if (
        root != null &&
            keys.all {
                current.arguments?.getString(it) != null &&
                    current.arguments?.getString(it) == root.arguments?.getString(it)
            }
    ) {
        popBackStack(RootDestination.TreatmentDoseFrequency.route, inclusive = false)
        return
    }
    fun value(key: String) = requireNotNull(current.arguments?.getString(key))
    navigate(
        RootDestination.TreatmentDoseFrequency.createRoute(
            value("caregiverId"),
            value("olderAdultId"),
            value("olderAdultName"),
            value("medicationId"),
            value("medicationLabel"),
            value("treatmentId"),
            value("treatmentName"),
        )
    ) {
        popUpTo(current.destination.id) { inclusive = true }
        launchSingleTop = true
    }
    val draft = requireNotNull(currentBackStackEntry).savedStateHandle
    listOf("dosage", "frequency", "scheduleText", "instructions").forEach { key ->
        current.arguments?.getString(key)?.let { draft[key] = it }
    }
    current.arguments
        ?.takeIf { it.containsKey("reminderDelay") }
        ?.getInt("reminderDelay")
        ?.let { draft["followUpDelayMinutes"] = it.toString() }
}
