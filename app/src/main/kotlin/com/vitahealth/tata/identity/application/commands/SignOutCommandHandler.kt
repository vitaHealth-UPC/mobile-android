package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Requests remote revocation and clearing of the locally held session. */
class SignOutCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(@Suppress("UNUSED_PARAMETER") command: SignOutCommand = SignOutCommand): AppResult<Unit> =
        repository.signOut()
}
