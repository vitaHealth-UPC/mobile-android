package com.vitahealth.tata.omission.application.commands

import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.shared.common.result.AppResult

/** Remembers the adult display target without subscribing to public clinical topics. */
class FollowDoseRemindersCommandHandler(private val store: PushTargetStore) {
    operator fun invoke(command: FollowDoseRemindersCommand): AppResult<Unit> {
        val olderAdultId = command.olderAdultId.trim()
        if (olderAdultId.isBlank())
            return AppResult.Failure(
                "No se pudo identificar el seguimiento.",
                code = OmissionFailureCodes.INVALID_REFERENCE,
            )
        store.rememberOlderAdult(olderAdultId, command.olderAdultName.trim())
        return AppResult.Success(Unit)
    }
}
