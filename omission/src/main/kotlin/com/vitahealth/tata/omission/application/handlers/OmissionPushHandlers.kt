package com.vitahealth.tata.omission.application.handlers

import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.omission.application.PushTargetStore
import com.vitahealth.tata.omission.application.PushTopicSubscriptions
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommand
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommand
import com.vitahealth.tata.omission.application.queries.PushDestination
import com.vitahealth.tata.omission.application.queries.ResolvePushDestinationQuery
import com.vitahealth.tata.omission.domain.model.OmissionPushKind
import com.vitahealth.tata.omission.domain.model.OmissionPushTopics
import com.vitahealth.tata.shared.common.result.AppResult

/** Result is whether the device could follow the topic; false means push is not configured on this build. */
class FollowCaregiverAlertsCommandHandler(
    private val store: PushTargetStore,
    private val subscriptions: PushTopicSubscriptions,
) {
    operator fun invoke(command: FollowCaregiverAlertsCommand): AppResult<Boolean> {
        val caregiverId = command.caregiverId.trim()
        val olderAdultId = command.olderAdultId.trim()
        if (caregiverId.isBlank() || olderAdultId.isBlank()) return invalidReference()
        store.rememberCaregiver(olderAdultId, caregiverId)
        return AppResult.Success(subscriptions.subscribe(OmissionPushTopics.forCaregiversOf(olderAdultId)))
    }
}

class FollowDoseRemindersCommandHandler(
    private val store: PushTargetStore,
    private val subscriptions: PushTopicSubscriptions,
) {
    operator fun invoke(command: FollowDoseRemindersCommand): AppResult<Boolean> {
        val olderAdultId = command.olderAdultId.trim()
        if (olderAdultId.isBlank()) return invalidReference()
        store.rememberOlderAdult(olderAdultId, command.olderAdultName.trim())
        return AppResult.Success(subscriptions.subscribe(OmissionPushTopics.forOlderAdult(olderAdultId)))
    }
}

/** Null when this device never followed that older adult, so the notification just opens the app. */
class ResolvePushDestinationQueryHandler(private val store: PushTargetStore) {
    operator fun invoke(query: ResolvePushDestinationQuery): PushDestination? {
        val olderAdultId = query.olderAdultId.trim().takeIf { it.isNotBlank() } ?: return null
        return when (query.kind) {
            OmissionPushKind.CAREGIVER_ALERT -> store.caregiverFor(olderAdultId)
                ?.let { PushDestination.CaregiverAlerts(it, olderAdultId, query.medicationName?.takeIf(String::isNotBlank)) }
            OmissionPushKind.REINFORCED_REMINDER -> store.olderAdultName(olderAdultId)
                ?.let { PushDestination.OlderAdultHome(olderAdultId, it) }
        }
    }
}

private fun invalidReference() = AppResult.Failure(
    message = "No se pudo identificar el seguimiento.",
    code = OmissionFailureCodes.INVALID_REFERENCE,
)
