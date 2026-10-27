package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.shared.common.result.AppResult

/** Links a verified provider identity to the authenticated caregiver's existing Tata account. */
class LinkSocialIdentityCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(firebaseIdToken: String): AppResult<Unit> {
        if (firebaseIdToken.isBlank() || firebaseIdToken.length > 16384) {
            return AppResult.Failure("Invalid social credential", code = "INVALID_SOCIAL_CREDENTIAL")
        }
        return repository.linkSocial(firebaseIdToken)
    }
}
