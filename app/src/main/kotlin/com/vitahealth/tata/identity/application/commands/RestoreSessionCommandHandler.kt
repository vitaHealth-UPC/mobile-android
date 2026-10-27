package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
/** Restores the session through the port; this may rebind or invalidate its owner. */
class RestoreSessionCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(@Suppress("UNUSED_PARAMETER") command: RestoreSessionCommand = RestoreSessionCommand): AppResult<SessionSubject> =
        repository.current()
}
