package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
class RemoteSessionAccessRepositoryTest {
    @Test
    fun protectedEndpointsUseTheApiCreatedFromTheirOriginalSessionSnapshot() = runBlocking {
        for (operation in listOf("current", "registerPin", "signOut")) {
            val store =
                Store().apply {
                    save("captured", "2099-01-01T00:00:00Z", SessionOwner("adult", "OLDER_ADULT"))
                }
            var credential: String? = null
            var called: String? = null
            val remote =
                RemoteSessionAccessRepository(
                    api { _, _ -> error("Shared API must not be used") },
                    store,
                ) { captured ->
                    credential = captured.accessToken
                    api { method, _ ->
                        called = method
                        store.save(
                            "new-login",
                            "2099-01-01T00:00:00Z",
                            SessionOwner("other", "CAREGIVER"),
                        )
                        if (method == "current")
                            Response.success(CurrentSessionResponse("adult", "OLDER_ADULT"))
                        else Response.success<Unit>(204, null)
                    }
                }
            when (operation) {
                "current" -> assertTrue(remote.current() is AppResult.Failure)
                "registerPin" ->
                    assertEquals(
                        "SESSION_CHANGED",
                        (remote.registerPin("adult", "1234") as AppResult.Failure).code,
                    )
                else -> assertTrue(remote.signOut() is AppResult.Success)
            }
            assertEquals(operation, called)
            assertEquals("captured", credential)
            assertEquals("new-login", store.token)
        }
    }

    @Test
    fun pinRegistrationRejectsAnotherAdultAndCaregiverBeforeOpeningApi() = runBlocking {
        val store = Store()
        val remote = RemoteSessionAccessRepository(api { _, _ -> error("No API expected") }, store)
        assertEquals(
            "AUTHENTICATION_REQUIRED",
            (remote.registerPin("adult", "1234") as AppResult.Failure).code,
        )
        for (owner in
            listOf(SessionOwner("other", "OLDER_ADULT"), SessionOwner("adult", "CAREGIVER"))) {
            store.save("token", "2099-01-01T00:00:00Z", owner)
            assertEquals(
                "OLDER_ADULT_SESSION_REQUIRED",
                (remote.registerPin("adult", "1234") as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun delayedPasswordAndPinResponsesCannotReplaceAnotherLogin() = runBlocking {
        for (pin in listOf(false, true)) {
            val store = Store()
            val remote =
                RemoteSessionAccessRepository(
                    api { _, _ ->
                        store.save(
                            "new-login",
                            "2099-01-01T00:00:00Z",
                            SessionOwner("other", "CAREGIVER"),
                        )
                        Response.success(
                            SessionAccessResponse(
                                if (pin) null else "care",
                                if (pin) "adult" else null,
                                "old-response",
                                "2099-01-01T00:00:00Z",
                            )
                        )
                    },
                    store,
                )
            val result =
                if (pin) remote.signInWithPin("adult", "1234")
                else remote.signIn("a@b.com", "password")
            assertEquals("SESSION_CHANGED", (result as AppResult.Failure).code)
            assertEquals("new-login", store.token)
            assertEquals(SessionOwner("other", "CAREGIVER"), store.owner)
        }
    }

    @Test
    fun expiredAuthenticationCredentialsAreRejectedBeforePersistence() = runBlocking {
        val store = Store()
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(
                        SessionAccessResponse("care", null, "expired", "2000-01-01T00:00:00Z")
                    )
                },
                store,
            )
        assertEquals(
            "INVALID_RESPONSE",
            (remote.signIn("a@b.com", "password") as AppResult.Failure).code,
        )
        assertNull(store.token)
    }

    private class Store : SessionStore {
        var token: String? = null
        var owner: SessionOwner? = null

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            save(accessToken, expiresAt)
            this.owner = owner
        }

        override fun session(): StoredSession? =
            token?.let { StoredSession(it, Instant.parse("2099-01-01T00:00:00Z"), owner) }

        override fun accessToken() = token

        override fun save(accessToken: String, expiresAt: String) {
            token = accessToken
        }

        override fun clear() {
            token = null
            owner = null
        }
    }

    private fun api(call: (String, Array<out Any>) -> Any?): SessionApiService =
        java.lang.reflect.Proxy.newProxyInstance(
            SessionApiService::class.java.classLoader,
            arrayOf(SessionApiService::class.java),
        ) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as SessionApiService

    @Test
    fun `password access saves token before returning caregiver identity`() = runBlocking {
        val store = Store()
        val remote =
            RemoteSessionAccessRepository(
                api { _, args ->
                    assertEquals("care@example.com", (args[0] as SignInRequest).email)
                    Response.success(
                        SessionAccessResponse("care-1", null, "secret", "2099-01-01T00:00:00Z")
                    )
                },
                store,
            )
        val result = remote.signIn(" care@example.com ", "password") as AppResult.Success
        assertEquals("CAREGIVER", result.value.role)
        assertEquals("care-1", result.value.subjectId)
        assertEquals("secret", store.token)
        assertEquals(SessionOwner("care-1", "CAREGIVER"), store.owner)
    }

    @Test
    fun `pin access establishes older adult role`() = runBlocking {
        val store = Store()
        val remote =
            RemoteSessionAccessRepository(
                api { _, args ->
                    assertEquals("1234", (args[0] as PinAccessRequest).pin)
                    Response.success(
                        SessionAccessResponse(
                            null,
                            "adult-1",
                            "adult-token",
                            "2099-01-01T00:00:00Z",
                        )
                    )
                },
                store,
            )
        val result = remote.signInWithPin("adult-1", "1234") as AppResult.Success
        assertEquals("OLDER_ADULT", result.value.role)
        assertEquals("adult-token", store.token)
        assertEquals(SessionOwner("adult-1", "OLDER_ADULT"), store.owner)
    }

    @Test
    fun `locked pin preserves existing session and server code`() = runBlocking {
        val store = Store().apply { token = "existing" }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.error<SessionAccessResponse>(
                        403,
                        "{\"code\":\"PIN_LOCKED\",\"message\":\"locked\"}".toResponseBody(),
                    )
                },
                store,
            )
        val result = remote.signInWithPin("adult-1", "0000") as AppResult.Failure
        assertEquals("PIN_LOCKED", result.code)
        assertEquals("existing", store.token)
    }

    @Test
    fun `expired session is cleared`() = runBlocking {
        val store = Store().apply { token = "expired" }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ -> Response.error<CurrentSessionResponse>(401, "{}".toResponseBody()) },
                store,
            )
        assertTrue(remote.current() is AppResult.Failure)
        assertNull(store.token)
    }

    @Test
    fun `logout clears session after successful revocation`() = runBlocking {
        val store = Store().apply { token = "secret" }
        val remote =
            RemoteSessionAccessRepository(api { _, _ -> Response.success<Unit>(204, null) }, store)
        assertTrue(remote.signOut() is AppResult.Success)
        assertNull(store.token)
    }

    @Test
    fun `wrong authentication subject never replaces the existing session`() = runBlocking {
        val store = Store().apply { token = "existing" }
        val caregiver =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(
                        SessionAccessResponse(null, "adult-1", "wrong", "2099-01-01T00:00:00Z")
                    )
                },
                store,
            )
        assertEquals(
            "INVALID_RESPONSE",
            (caregiver.signIn("care@example.com", "password") as AppResult.Failure).code,
        )
        assertEquals("existing", store.token)

        val adult =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(
                        SessionAccessResponse("care-1", null, "wrong", "2099-01-01T00:00:00Z")
                    )
                },
                store,
            )
        assertEquals(
            "INVALID_RESPONSE",
            (adult.signInWithPin("adult-1", "1234") as AppResult.Failure).code,
        )
        assertEquals("existing", store.token)
    }

    @Test
    fun `invalid expiry never replaces the existing session`() = runBlocking {
        val store = Store().apply { token = "existing" }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(SessionAccessResponse("care-1", null, "wrong", "invalid"))
                },
                store,
            )
        assertEquals(
            "INVALID_RESPONSE",
            (remote.signIn("care@example.com", "password") as AppResult.Failure).code,
        )
        assertEquals("existing", store.token)
    }

    @Test
    fun `current session rejects unknown roles and blank subjects`() = runBlocking {
        for (body in
            listOf(
                CurrentSessionResponse("care-1", "UNKNOWN"),
                CurrentSessionResponse(" ", "CAREGIVER"),
            )) {
            val store = Store().apply { token = "existing" }
            val remote =
                RemoteSessionAccessRepository(api { _, _ -> Response.success(body) }, store)
            assertEquals("INVALID_RESPONSE", (remote.current() as AppResult.Failure).code)
            assertEquals("existing", store.token)
        }
    }

    @Test
    fun legacySessionBindsOnlyTheSubjectValidatedByCurrentEndpoint() = runBlocking {
        val store = Store().apply { token = "legacy" }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ -> Response.success(CurrentSessionResponse("care-1", "CAREGIVER")) },
                store,
            )
        assertTrue(remote.current() is AppResult.Success)
        assertEquals("legacy", store.token)
        assertEquals(SessionOwner("care-1", "CAREGIVER"), store.owner)
    }

    @Test
    fun staleCurrentResponseCannotBindANewerLogin() = runBlocking {
        val store = Store().apply { token = "previous" }
        val newerOwner = SessionOwner("new-account", "CAREGIVER")
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    store.save("new-token", "2099-01-01T00:00:00Z", newerOwner)
                    Response.success(CurrentSessionResponse("old-account", "CAREGIVER"))
                },
                store,
            )
        assertEquals("INVALID_RESPONSE", (remote.current() as AppResult.Failure).code)
        assertEquals("new-token", store.token)
        assertEquals(newerOwner, store.owner)
    }

    @Test
    fun staleUnauthorizedResponseCannotClearANewerLogin() = runBlocking {
        val store = Store().apply { token = "previous" }
        val newerOwner = SessionOwner("new-account", "CAREGIVER")
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    store.save("new-token", "2099-01-01T00:00:00Z", newerOwner)
                    Response.error<CurrentSessionResponse>(401, "{}".toResponseBody())
                },
                store,
            )
        assertTrue(remote.current() is AppResult.Failure)
        assertEquals("new-token", store.token)
        assertEquals(newerOwner, store.owner)
    }

    @Test
    fun delayedLogoutCannotClearANewerLogin() = runBlocking {
        val store = Store().apply { token = "previous" }
        val newerOwner = SessionOwner("new-account", "CAREGIVER")
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    store.save("new-token", "2099-01-01T00:00:00Z", newerOwner)
                    Response.success<Unit>(204, null)
                },
                store,
            )
        assertTrue(remote.signOut() is AppResult.Success)
        assertEquals("new-token", store.token)
        assertEquals(newerOwner, store.owner)
    }

    @Test
    fun sameTokenCannotBeReboundToAnotherSubject() = runBlocking {
        val owner = SessionOwner("care-1", "CAREGIVER")
        val store = Store().apply { save("existing", "2099-01-01T00:00:00Z", owner) }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(CurrentSessionResponse("other-account", "CAREGIVER"))
                },
                store,
            )
        assertEquals("INVALID_RESPONSE", (remote.current() as AppResult.Failure).code)
        assertEquals(owner, store.owner)
    }

    @Test
    fun pinResponseForAnotherAdultCannotReplaceExistingCredentials() = runBlocking {
        val store = Store().apply { token = "existing" }
        val remote =
            RemoteSessionAccessRepository(
                api { _, _ ->
                    Response.success(
                        SessionAccessResponse(null, "other-adult", "wrong", "2099-01-01T00:00:00Z")
                    )
                },
                store,
            )
        assertEquals(
            "INVALID_RESPONSE",
            (remote.signInWithPin("adult-1", "1234") as AppResult.Failure).code,
        )
        assertEquals("existing", store.token)
    }

    @Test
    fun socialSignInExchangesOpaqueFirebaseEvidenceAndPersistsOnlyTataSession() = runBlocking {
        val store = Store()
        val remote =
            RemoteSessionAccessRepository(
                api { operation, args ->
                    assertEquals("signInSocial", operation)
                    assertEquals("signed-firebase-evidence", (args[0] as SocialSessionRequest).firebaseIdToken)
                    assertFalse(args[0].toString().contains("signed-firebase-evidence"))
                    Response.success(
                        SessionAccessResponse("linked-caregiver", null, "tata-session",
                            "2099-01-01T00:00:00Z")
                    )
                },
                store,
            )
        val result = remote.signInSocial("signed-firebase-evidence") as AppResult.Success
        assertEquals("linked-caregiver", result.value.subjectId)
        assertEquals("CAREGIVER", result.value.role)
        assertEquals("tata-session", store.token)
        assertEquals(SessionOwner("linked-caregiver", "CAREGIVER"), store.owner)
    }

    @Test
    fun socialSignInRejectsInvalidEvidenceAndLateResponsesWithoutReplacingSession() = runBlocking {
        val store = Store()
        var called = false
        val remote =
            RemoteSessionAccessRepository(api { _, _ ->
                called = true
                store.save("another-user", "2099-01-01T00:00:00Z",
                    SessionOwner("other", "CAREGIVER"))
                Response.success(
                    SessionAccessResponse("linked", null, "stale-social-session",
                        "2099-01-01T00:00:00Z"))
            }, store)
        assertEquals("INVALID_SOCIAL_CREDENTIAL",
            (remote.signInSocial("") as AppResult.Failure).code)
        assertEquals("INVALID_SOCIAL_CREDENTIAL",
            (remote.signInSocial("x".repeat(16385)) as AppResult.Failure).code)
        assertFalse(called)
        assertEquals("SESSION_CHANGED",
            (remote.signInSocial("valid-firebase-evidence") as AppResult.Failure).code)
        assertEquals("another-user", store.token)
    }


    @Test
    fun socialAccountLinkUsesOnlyAnAuthorizedCaregiverSessionAndPreservesIt() = runBlocking {
        val store = Store().apply {
            save("caregiver-session", "2099-01-01T00:00:00Z",
                SessionOwner("caregiver-1", "CAREGIVER"))
        }
        var invoked = 0
        val remote = RemoteSessionAccessRepository(
            api { _, _ -> error("Never use the shared API for authenticated writes") },
            store,
        ) { captured ->
            assertEquals("caregiver-session", captured.accessToken)
            api { operation, args ->
                assertEquals("linkSocial", operation)
                assertEquals("signed-provider-credential",
                    (args[0] as SocialSessionRequest).firebaseIdToken)
                invoked++
                Response.success<Unit>(204, null)
            }
        }
        assertTrue(remote.linkSocial("signed-provider-credential") is AppResult.Success)
        assertEquals(1, invoked)
        assertEquals("caregiver-session", store.token)
        assertEquals(SessionOwner("caregiver-1", "CAREGIVER"), store.owner)
    }

    @Test
    fun socialAccountLinkRejectsMissingOrAdultSessionAndLateResponses() = runBlocking {
        val store = Store()
        val remote = RemoteSessionAccessRepository(
            api { _, _ -> error("Network must not be invoked without caregiver credentials") },
            store,
        )
        assertEquals("AUTHENTICATION_REQUIRED",
            (remote.linkSocial("signed") as AppResult.Failure).code)
        store.save("adult-session", "2099-01-01T00:00:00Z",
            SessionOwner("adult", "OLDER_ADULT"))
        assertEquals("AUTHENTICATION_REQUIRED",
            (remote.linkSocial("signed") as AppResult.Failure).code)

        store.save("caregiver-session", "2099-01-01T00:00:00Z",
            SessionOwner("caregiver", "CAREGIVER"))
        val late = RemoteSessionAccessRepository(
            api { _, _ -> error("No shared API") }, store,
        ) { _ ->
            api { operation, _ ->
                assertEquals("linkSocial", operation)
                store.save("new-owner", "2099-01-01T00:00:00Z",
                    SessionOwner("other", "CAREGIVER"))
                Response.success<Unit>(204, null)
            }
        }
        assertEquals("SESSION_CHANGED",
            (late.linkSocial("signed") as AppResult.Failure).code)
        assertEquals("new-owner", store.token)
    }

}
