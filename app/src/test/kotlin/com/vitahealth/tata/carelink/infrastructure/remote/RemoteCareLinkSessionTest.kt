package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteCareLinkSessionTest {
    @Test
    fun allOperationsUseTheCapturedApiAndRejectResponsesAfterAccountReplacement() = runBlocking {
        for (operation in listOf("accept", "consent", "profile")) {
            val sessions = RecordingSessionStore().apply { token = "original" }
            var capturedToken: String? = null
            val unused =
                java.lang.reflect.Proxy.newProxyInstance(
                    CareLinkApiService::class.java.classLoader,
                    arrayOf(CareLinkApiService::class.java),
                ) { _, _, _ ->
                    error("Dynamic API must not be used")
                } as CareLinkApiService
            val repository =
                RemoteCareLinkRepository(unused, sessions) { captured ->
                    capturedToken = captured?.accessToken
                    java.lang.reflect.Proxy.newProxyInstance(
                        CareLinkApiService::class.java.classLoader,
                        arrayOf(CareLinkApiService::class.java),
                    ) { _, _, _ ->
                        sessions.save(
                            "replacement",
                            "2099-01-01T00:00:00Z",
                            SessionOwner("other", "CAREGIVER"),
                        )
                        Response.success<String>(204, null)
                    } as CareLinkApiService
                }
            val result =
                when (operation) {
                    "accept" -> repository.acceptLink("caregiver", "123456")
                    "consent" -> repository.registerConsent("link", true)
                    else -> repository.getOlderAdult("adult")
                }
            assertEquals("original", capturedToken)
            assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
            assertEquals("replacement", sessions.token)
        }
    }

    @Test
    fun aNewLoginDuringEitherOnboardingRequestIsNotOverwritten() = runBlocking {
        for (consent in listOf(false, true)) {
            val sessions = RecordingSessionStore().apply { token = "original" }
            val api =
                java.lang.reflect.Proxy.newProxyInstance(
                    CareLinkApiService::class.java.classLoader,
                    arrayOf(CareLinkApiService::class.java),
                ) { _, _, _ ->
                    sessions.save(
                        "new-login",
                        "2099-01-01T00:00:00Z",
                        SessionOwner("other", "CAREGIVER"),
                    )
                    Response.success(
                        CareLinkResponse(
                            "link",
                            "caregiver",
                            "adult",
                            if (consent) "CONFIRMED" else "AWAITING_CONSENT",
                            null,
                            null,
                            null,
                            consent,
                            null,
                            null,
                            "old-response-token",
                            "2099-01-01T00:00:00Z",
                        )
                    )
                } as CareLinkApiService
            val repository = RemoteCareLinkRepository(api, sessions)
            val result =
                if (consent) repository.registerConsent("link", true)
                else repository.acceptLink("caregiver", "123456")
            assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
            assertEquals("new-login", sessions.token)
            assertEquals(SessionOwner("other", "CAREGIVER"), sessions.owner)
        }
    }

    @Test
    fun conditionalReplacementChecksOwnerAsWellAsBearerAndHandlesSignedOutState() {
        val sessions = RecordingSessionStore()
        val replacement =
            StoredSession(
                "adult-token",
                Instant.parse("2099-01-01T00:00:00Z"),
                SessionOwner("adult", "OLDER_ADULT"),
            )
        assertTrue(sessions.replaceIfCurrent(null, replacement))
        val expected = sessions.session()
        sessions.save("adult-token", "2099-01-01T00:00:00Z", SessionOwner("other", "OLDER_ADULT"))
        assertFalse(sessions.replaceIfCurrent(expected, replacement))
        assertFalse(sessions.replaceIfCurrent(null, replacement))
        sessions.clear()
        assertFalse(sessions.replaceIfCurrent(expected, replacement))
    }

    @Test
    fun inconsistentConsentReceiptCannotReplaceSessionCredentials() = runBlocking {
        val valid =
            CareLinkResponse(
                "link",
                "caregiver",
                "adult",
                "CONFIRMED",
                null,
                null,
                null,
                true,
                null,
                "2026-10-07T00:00:00Z",
                "replacement",
                "2099-01-01T00:00:00Z",
            )
        for ((accepted, body) in
            listOf(
                true to valid.copy(consentGranted = false),
                true to valid.copy(consentGranted = null),
                true to valid.copy(status = "AWAITING_CONSENT"),
                false to valid,
                false to valid.copy(consentGranted = false),
                false to valid.copy(consentGranted = false, status = "REVOKED"),
            )) {
            val api =
                java.lang.reflect.Proxy.newProxyInstance(
                    CareLinkApiService::class.java.classLoader,
                    arrayOf(CareLinkApiService::class.java),
                ) { _, _, _ ->
                    Response.success(body)
                } as CareLinkApiService
            val sessions = RecordingSessionStore().apply { token = "original" }
            val result = RemoteCareLinkRepository(api, sessions).registerConsent("link", accepted)
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
            assertEquals("original", sessions.token)
        }
    }

    @Test
    fun omittedConsentFlagIsNotConvertedToAnExplicitRejection() {
        val json =
            """{"id":"link","caregiverId":"caregiver","olderAdultId":"adult","status":"PENDING"}"""
        val body = com.google.gson.Gson().fromJson(json, CareLinkResponse::class.java)
        assertNull(body.consentGranted)
        assertThrows(IllegalArgumentException::class.java) { body.toDomain() }
    }

    private class RecordingSessionStore : SessionStore {
        var token: String? = null
        var owner: SessionOwner? = null

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            save(accessToken, expiresAt)
            this.owner = owner
        }

        override fun session(): StoredSession? =
            token?.let { StoredSession(it, Instant.parse("2099-01-01T00:00:00Z"), owner) }

        override fun save(accessToken: String, expiresAt: String) {
            token = accessToken
        }

        override fun accessToken(): String? = token

        override fun clear() {
            token = null
            owner = null
        }
    }

    private class Api : CareLinkApiService {
        private fun link(token: String, confirmed: Boolean) =
            CareLinkResponse(
                "link",
                "caregiver",
                "adult",
                if (confirmed) "CONFIRMED" else "PENDING",
                null,
                null,
                "2026-10-07T00:00:00Z",
                confirmed,
                null,
                if (confirmed) "2026-10-07T00:00:00Z" else null,
                token,
                "2099-01-01T00:00:00Z",
            )

        override suspend fun acceptLink(request: AcceptCareLinkRequest) =
            Response.success(link("setup-token", false))

        override suspend fun registerConsent(careLinkId: String, request: RegisterConsentRequest) =
            Response.success(link("adult-token", true))

        override suspend fun getOlderAdult(
            olderAdultId: String
        ): Response<OlderAdultProfileResponse> = throw UnsupportedOperationException()
    }

    @Test
    fun consentReplacesRestrictedSetupSessionWithAdultSession() = runBlocking {
        val sessions = RecordingSessionStore()
        val repository = RemoteCareLinkRepository(Api(), sessions)
        assertTrue(repository.acceptLink("caregiver", "123456") is AppResult.Success)
        assertEquals("setup-token", sessions.token)
        assertEquals(SessionOwner("adult", "LINK_SETUP"), sessions.owner)
        assertTrue(repository.registerConsent("link", true) is AppResult.Success)
        assertEquals("adult-token", sessions.token)
        assertEquals(SessionOwner("adult", "OLDER_ADULT"), sessions.owner)
    }

    @Test
    fun malformedLinkDoesNotReplaceAnExistingSession() = runBlocking {
        val valid =
            CareLinkResponse(
                "link",
                "caregiver",
                "adult",
                "PENDING",
                null,
                null,
                null,
                false,
                null,
                null,
                "new-token",
                "2099-01-01T00:00:00Z",
            )
        for (body in
            listOf(
                valid.copy(status = "UNKNOWN"),
                valid.copy(codeUsedAt = "invalid"),
                valid.copy(expiresAt = null),
                valid.copy(id = ""),
                valid.copy(caregiverId = "another-caregiver"),
            )) {
            val api =
                object : CareLinkApiService {
                    override suspend fun acceptLink(request: AcceptCareLinkRequest) =
                        Response.success(body)

                    override suspend fun registerConsent(
                        careLinkId: String,
                        request: RegisterConsentRequest,
                    ) = Response.success(body)

                    override suspend fun getOlderAdult(
                        olderAdultId: String
                    ): Response<OlderAdultProfileResponse> = error("Unused")
                }
            val sessions = RecordingSessionStore().apply { token = "existing" }
            val result = RemoteCareLinkRepository(api, sessions).acceptLink("caregiver", "123456")
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
            assertEquals("existing", sessions.token)
        }
    }

    @Test
    fun consentForAnotherLinkDoesNotReplaceAnExistingSession() = runBlocking {
        val sessions = RecordingSessionStore().apply { token = "existing" }
        val repository = RemoteCareLinkRepository(Api(), sessions)

        val result = repository.registerConsent("another-link", true)

        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        assertEquals("existing", sessions.token)
    }

    @Test
    fun profileForAnotherAdultIsRejected() = runBlocking {
        val api =
            object : CareLinkApiService {
                override suspend fun acceptLink(
                    request: AcceptCareLinkRequest
                ): Response<CareLinkResponse> = error("Unused")

                override suspend fun registerConsent(
                    careLinkId: String,
                    request: RegisterConsentRequest,
                ): Response<CareLinkResponse> = error("Unused")

                override suspend fun getOlderAdult(olderAdultId: String) =
                    Response.success(
                        OlderAdultProfileResponse(
                            id = "another-adult",
                            registeredByCaregiverId = "caregiver",
                            fullName = "Rosa Vargas",
                            birthDate = "1958-01-01",
                            emergencyContactName = null,
                            emergencyContactRelationship = null,
                            emergencyContactPhone = null,
                            createdAt = null,
                        )
                    )
            }

        val result = RemoteCareLinkRepository(api).getOlderAdult("adult")

        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }
}
