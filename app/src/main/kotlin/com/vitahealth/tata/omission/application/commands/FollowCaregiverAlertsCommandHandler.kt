package com.vitahealth.tata.omission.application.commands

import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.shared.common.result.AppResult

/** Remembers a navigation target; clinical delivery uses authenticated device registration. */
class FollowCaregiverAlertsCommandHandler(private val store: PushTargetStore) {
    operator fun invoke(command: FollowCaregiverAlertsCommand): AppResult<Unit> {
        val caregiverId = command.caregiverId.trim()
        val olderAdultId = command.olderAdultId.trim()
        if (caregiverId.isBlank() || olderAdultId.isBlank())
            return AppResult.Failure(
                "No se pudo identificar el seguimiento.",
                code = OmissionFailureCodes.INVALID_REFERENCE,
            )
        store.rememberCaregiver(olderAdultId, caregiverId)
        return AppResult.Success(Unit)
    }
}
