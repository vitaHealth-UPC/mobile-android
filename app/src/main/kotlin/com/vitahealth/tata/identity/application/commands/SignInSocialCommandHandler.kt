package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.SessionAccessRepository
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult

/** Exchanges a provider-signed Firebase ID token for a revocable Tata caregiver session. */
class SignInSocialCommandHandler(private val repository: SessionAccessRepository) {
    suspend operator fun invoke(firebaseIdToken: String): AppResult<SessionSubject> {
        if (firebaseIdToken.isBlank() || firebaseIdToken.length > 16384) {
            return AppResult.Failure("Invalid social credential", code = "INVALID_SOCIAL_CREDENTIAL")
        }
        return repository.signInSocial(firebaseIdToken)
    }
}
