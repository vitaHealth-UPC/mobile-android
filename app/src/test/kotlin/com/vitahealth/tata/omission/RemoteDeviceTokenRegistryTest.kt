package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.infrastructure.remote.PushDeviceApiService
import com.vitahealth.tata.omission.infrastructure.remote.RegisterPushDeviceRequest
import com.vitahealth.tata.omission.infrastructure.remote.RemoteDeviceTokenRegistry
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.time.Instant
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Verifies the real installation HTTP contract without Firebase credentials or live writes. */
class RemoteDeviceTokenRegistryTest {
    private val installation = "00000000-0000-0000-0000-000000000001"
    private val owner = SessionOwner("caregiver", "CAREGIVER")
    private val server = MockWebServer()
    private val sessions = MemorySessions()

    @Before
    fun setUp() {
        server.start()
        sessions.current =
            StoredSession("account-token", Instant.parse("2099-01-01T00:00:00Z"), owner)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun api(session: StoredSession): PushDeviceApiService =
        ApiClientFactory.create(server.url("/").toString()) { session.accessToken }
            .create(PushDeviceApiService::class.java)

    private fun registry() = RemoteDeviceTokenRegistry(sessions, installation, ::api)

    @Test
    fun anObsoleteCoordinatorSnapshotCannotRegisterOrRemoveTheNewSession() = runTest {
        val previous = requireNotNull(sessions.current)
        sessions.current =
            StoredSession("new-token", Instant.MAX, SessionOwner("other", "CAREGIVER"))
        val registry =
            RemoteDeviceTokenRegistry(sessions, installation, { error("No API expected") })
        assertEquals(
            "SESSION_CHANGED",
            (registry.register("fcm-token", previous) as AppResult.Failure).code,
        )
        assertEquals("SESSION_CHANGED", (registry.unregister(previous) as AppResult.Failure).code)
    }

    @Test
    fun registrationUsesPutAndTokenOnlyBodyWithCapturedBearer() = runTest {
        server.enqueue(
            MockResponse()
                .setBody(
                    """{"installationId":"$installation","updatedAt":"2026-10-23T12:00:00Z"}"""
                )
        )
        assertTrue(registry().register("fcm-token") is AppResult.Success)
        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/push-devices/$installation", request.path)
        assertEquals("Bearer account-token", request.getHeader("Authorization"))
        val body = com.google.gson.JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(setOf("token"), body.keySet())
        assertEquals("fcm-token", body["token"].asString)
        assertFalse(RegisterPushDeviceRequest("secret-value").toString().contains("secret-value"))
    }

    @Test
    fun wrongIdentityAbsentBodyAndInvalidTimestampRejectTheReceipt() = runTest {
        for (body in
            listOf(
                """{"installationId":"other","updatedAt":"2026-10-23T12:00:00Z"}""",
                """{"installationId":"$installation","updatedAt":"invalid"}""",
                "null",
            )) {
            server.enqueue(MockResponse().setBody(body))
            assertEquals(
                "INVALID_RESPONSE",
                (registry().register("fcm-token") as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun unregisterRequiresTheNoContentContract() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))
        assertTrue(registry().unregister() is AppResult.Success)
        assertEquals("DELETE", server.takeRequest().method)
        server.enqueue(MockResponse().setResponseCode(200))
        assertEquals("INVALID_RESPONSE", (registry().unregister() as AppResult.Failure).code)
    }

    @Test
    fun invalidTokenAndUnauthenticatedSessionsNeverCreateAnApi() = runTest {
        val registry =
            RemoteDeviceTokenRegistry(sessions, installation, { error("No API expected") })
        for (token in listOf(" ", "x".repeat(4097))) {
            assertEquals(
                "INVALID_DEVICE_TOKEN",
                (registry.register(token) as AppResult.Failure).code,
            )
        }
        for (session in
            listOf(
                null,
                StoredSession("expired", Instant.EPOCH, owner),
                StoredSession("setup", Instant.MAX, SessionOwner("adult", "LINK_SETUP")),
            )) {
            sessions.current = session
            assertEquals(
                "SESSION_REQUIRED",
                (registry.register("fcm-token") as AppResult.Failure).code,
            )
            assertEquals("SESSION_REQUIRED", (registry.unregister() as AppResult.Failure).code)
        }
    }

    @Test
    fun accountChangeDoesNotReplaceTheCapturedBearerAndDiscardsReceipt() = runTest {
        server.enqueue(
            MockResponse()
                .setBody(
                    """{"installationId":"$installation","updatedAt":"2026-10-23T12:00:00Z"}"""
                )
        )
        val registry =
            RemoteDeviceTokenRegistry(
                sessions,
                installation,
                { captured ->
                    sessions.current =
                        StoredSession("new-token", Instant.MAX, SessionOwner("other", "CAREGIVER"))
                    api(captured)
                },
            )
        assertEquals("SESSION_CHANGED", (registry.register("fcm-token") as AppResult.Failure).code)
        assertEquals("Bearer account-token", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun authorizationFailuresAreNotReportedAsMissingEndpoint() = runTest {
        for ((status, code) in
            listOf(401 to "SESSION_REQUIRED", 403 to "ACCESS_DENIED", 503 to "REQUEST_FAILED")) {
            server.enqueue(MockResponse().setResponseCode(status))
            assertEquals(code, (registry().register("fcm-token") as AppResult.Failure).code)
        }
    }

    private class MemorySessions : SessionStore {
        var current: StoredSession? = null

        override fun session() = current

        override fun accessToken() = current?.accessToken

        override fun save(accessToken: String, expiresAt: String) {
            current = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            current = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }

        override fun clear() {
            current = null
        }
    }
}
