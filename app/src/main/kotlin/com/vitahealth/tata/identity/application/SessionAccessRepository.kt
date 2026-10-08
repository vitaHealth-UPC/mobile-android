package com.vitahealth.tata.identity.application

import com.vitahealth.tata.shared.common.result.AppResult

data class SessionSubject(val subjectId: String, val role: String)
interface SessionAccessRepository {
    suspend fun signIn(email: String, password: String): AppResult<SessionSubject>
    suspend fun signInWithPin(olderAdultId: String, pin: String): AppResult<SessionSubject>
    suspend fun registerPin(olderAdultId: String, pin: String): AppResult<Unit>
    suspend fun current(): AppResult<SessionSubject>
    suspend fun signOut(): AppResult<Unit>
}
