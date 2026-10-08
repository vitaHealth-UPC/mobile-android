package com.vitahealth.tata.identity.application

import com.vitahealth.tata.shared.common.result.AppResult

class FakeSessionAccessRepository : SessionAccessRepository {
    var result: AppResult<SessionSubject> = AppResult.Success(SessionSubject("caregiver-1", "CAREGIVER"))
    var registerPinResult: AppResult<Unit> = AppResult.Success(Unit)
    val signIns = mutableListOf<Pair<String, String>>()
    val pinSignIns = mutableListOf<Pair<String, String>>()
    val pinRegistrations = mutableListOf<Pair<String, String>>()
    var currentCalls = 0
    var signOuts = 0

    override suspend fun signIn(email: String, password: String): AppResult<SessionSubject> {
        signIns += email to password
        return result
    }

    override suspend fun signInWithPin(olderAdultId: String, pin: String): AppResult<SessionSubject> {
        pinSignIns += olderAdultId to pin
        return result
    }

    override suspend fun registerPin(olderAdultId: String, pin: String): AppResult<Unit> {
        pinRegistrations += olderAdultId to pin
        return registerPinResult
    }

    override suspend fun current(): AppResult<SessionSubject> {
        currentCalls++
        return result
    }

    override suspend fun signOut(): AppResult<Unit> {
        signOuts++
        return AppResult.Success(Unit)
    }
}
