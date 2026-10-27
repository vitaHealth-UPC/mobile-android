package com.vitahealth.tata.carelink.infrastructure.security

import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.lang.reflect.Proxy
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies access checks and late-account rejection for all three profile operations. */
class SessionScopedCaregiverProfilesRepositoryTest {
    private val now = Instant.parse("2026-10-23T12:00:00Z")
    private val owner = SessionOwner("caregiver", "CAREGIVER")

    private fun session(account: SessionOwner = owner) =
        StoredSession("original", now.plusSeconds(60), account)

    @Test
    fun absentExpiredAndWrongAccountNeverOpenTheRemote() = runBlocking {
        for (value in
            listOf(
                null,
                StoredSession("expired", now, owner),
                session(SessionOwner("other", "CAREGIVER")),
                session(SessionOwner("caregiver", "OLDER_ADULT")),
            )) {
            val repository =
                SessionScopedCaregiverProfilesRepository(
                    Store(value),
                    "caregiver",
                    { error("Unexpected remote") },
                ) {
                    now
                }
            for (operation in 0..2) {
                val result = invoke(repository, operation) as AppResult.Failure
                assertEquals(
                    if (value == null || !value.expiresAt.isAfter(now)) "AUTHENTICATION_REQUIRED"
                    else "RESOURCE_ACCESS_DENIED",
                    result.code,
                )
            }
        }
    }

    @Test
    fun accountReplacementDiscardsEachOperationsResponseUsingOriginalCredentials() = runBlocking {
        for (operation in 0..2) {
            val store = Store(session())
            var token: String? = null
            val repository =
                SessionScopedCaregiverProfilesRepository(
                    store,
                    "caregiver",
                    { captured ->
                        token = captured.accessToken
                        Proxy.newProxyInstance(
                            CaregiverProfilesRepository::class.java.classLoader,
                            arrayOf(CaregiverProfilesRepository::class.java),
                        ) { _, _, _ ->
                            store.value = session(SessionOwner("other", "CAREGIVER"))
                            AppResult.Failure("Old response", code = "REQUEST_FAILED")
                        } as CaregiverProfilesRepository
                    },
                ) {
                    now
                }
            assertEquals(
                "SESSION_CHANGED",
                (invoke(repository, operation) as AppResult.Failure).code,
            )
            assertEquals("original", token)
            assertEquals("other", store.value?.owner?.subjectId)
        }
    }

    private suspend fun invoke(
        repository: CaregiverProfilesRepository,
        operation: Int,
    ): AppResult<*> =
        when (operation) {
            0 -> repository.list("caregiver")
            1 ->
                repository.register(
                    "caregiver",
                    NewAdultProfile(
                        "Rosa",
                        LocalDate.parse("1958-01-01"),
                        "Diego",
                        "Hijo",
                        "999888777",
                    ),
                )
            else -> repository.linkingCode("caregiver", LinkedAdult("adult", "Rosa"))
        }

    private class Store(var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun clear() {
            value = null
        }

        override fun save(accessToken: String, expiresAt: String) {
            value = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            value = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }
    }
}
