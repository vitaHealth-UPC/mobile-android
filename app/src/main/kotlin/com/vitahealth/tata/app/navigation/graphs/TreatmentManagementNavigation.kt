package com.vitahealth.tata.app.navigation.graphs

import androidx.navigation.NavHostController
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

/** Opens management using a successful feature read; all mutations retain server authorization. */
internal fun NavHostController.openTreatmentManagement(
    caregiverId: String,
    olderAdultName: String,
    detail: TreatmentDetailReadModel,
    medicationLabel: String,
) {
    val medicationId = detail.medicationId ?: return
    val dose = detail.dose ?: return
    val frequency = detail.frequency ?: return
    if (detail.scheduledTimes.isEmpty()) return
    navigate(
        RootDestination.TreatmentLifecycle.createRoute(
            caregiverId,
            detail.olderAdultId,
            olderAdultName,
            medicationId,
            medicationLabel,
            detail.id,
            detail.name,
            dose,
            frequency,
            detail.scheduledTimes.joinToString(","),
            detail.instructions,
            detail.followUpDelayMinutes ?: 0,
            initialStatus = detail.status.name,
        )
    ) {
        launchSingleTop = true
    }
}
