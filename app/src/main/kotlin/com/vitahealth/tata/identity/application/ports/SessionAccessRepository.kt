package com.vitahealth.tata.identity.application.ports

import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
/** Session access and revocation capability used by the access flow. */
interface SessionAccessRepository {
    suspend fun signIn(email: String, password: String): AppResult<SessionSubject>

    /** Verified external credentials are always exchanged with Tata; no Firebase token is stored. */
    suspend fun signInSocial(firebaseIdToken: String): AppResult<SessionSubject> =
        AppResult.Failure("Social authentication is unavailable", code = "SOCIAL_UNAVAILABLE")

    /** Existing caregiver account linking requires the current authenticated Tata session. */
    suspend fun linkSocial(firebaseIdToken: String): AppResult<Unit> =
        AppResult.Failure("Social linking is unavailable", code = "SOCIAL_UNAVAILABLE")

    suspend fun signInWithPin(olderAdultId: String, pin: String): AppResult<SessionSubject>

    suspend fun registerPin(olderAdultId: String, pin: String): AppResult<Unit>

    suspend fun current(): AppResult<SessionSubject>

    suspend fun signOut(): AppResult<Unit>
}
