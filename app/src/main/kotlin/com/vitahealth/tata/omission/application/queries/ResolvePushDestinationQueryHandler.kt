package com.vitahealth.tata.omission.application.queries

import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind

/** Resolves exact intake references and legacy targets before separate session authorization. */
class ResolvePushDestinationQueryHandler(private val store: PushTargetStore) {
    operator fun invoke(query: ResolvePushDestinationQuery): PushDestination? {
        val olderAdultId = query.olderAdultId.trim().takeIf { it.isNotBlank() } ?: return null
        val intakeId = query.intakeId?.trim()
        if (intakeId != null && intakeId.isBlank()) return null
        return when (query.kind) {
            OmissionPushKind.CAREGIVER_ALERT ->
                store.caregiverFor(olderAdultId)?.let {
                    PushDestination.CaregiverAlerts(
                        it,
                        olderAdultId,
                        query.medicationName?.takeIf(String::isNotBlank),
                        intakeId,
                    )
                }
            OmissionPushKind.REINFORCED_REMINDER ->
                intakeId?.let { PushDestination.OlderAdultDose(olderAdultId, it) }
                    ?: store.olderAdultName(olderAdultId)?.let {
                        PushDestination.OlderAdultHome(olderAdultId, it)
                    }
        }
    }
}
