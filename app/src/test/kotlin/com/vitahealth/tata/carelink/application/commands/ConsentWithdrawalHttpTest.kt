package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.shared.application.session.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class ConsentWithdrawalHttpTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")

    private class Store(var value: StoredSession?) : SessionStore {
        override fun session() = value

        override fun accessToken() = value?.accessToken

        override fun save(accessToken: String, expiresAt: String) = Unit

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) = Unit

        override fun clear() {
            value = null
        }
    }

    private fun session() =
        StoredSession("captured", now.plusSeconds(300), SessionOwner("caregiver", "CAREGIVER"))

    @Test
    fun resolvesOnlyAuthenticatedAdultLinkBeforeWithdrawal() = runBlocking {
        MockWebServer().use { server ->
            val store =
                Store(
                    StoredSession(
                        "captured",
                        now.plusSeconds(300),
                        SessionOwner("adult", "OLDER_ADULT"),
                    )
                )
            val writer =
                com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedConsentWithdrawal(
                    store,
                    server.url("/").toString(),
                ) {
                    now
                }
            server.enqueue(
                MockResponse()
                    .setBody("""{"subjectId":"adult","role":"OLDER_ADULT","careLinkId":"link"}""")
            )
            server.enqueue(MockResponse().setResponseCode(204))
            assertTrue(writer.withdraw("adult") is AppResult.Success)
            assertEquals("/api/v1/sessions/current", server.takeRequest().path)
            val write = server.takeRequest()
            assertEquals("DELETE", write.method)
            assertEquals("/api/v1/care-links/link/consent", write.path)
            assertEquals("Bearer captured", write.getHeader("Authorization"))
            assertEquals(0L, write.bodySize)
            // This adapter never signs out a potentially newer session.
            assertNotNull(store.value)
        }
    }

    @Test
    fun missingOrForeignSessionEvidenceCannotDispatchWithdrawal() = runBlocking {
        MockWebServer().use { server ->
            val store =
                Store(
                    StoredSession(
                        "captured",
                        now.plusSeconds(300),
                        SessionOwner("adult", "OLDER_ADULT"),
                    )
                )
            val writer =
                com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedConsentWithdrawal(
                    store,
                    server.url("/").toString(),
                ) {
                    now
                }
            for (json in
                listOf(
                    """{"subjectId":"other","role":"OLDER_ADULT","careLinkId":"link"}""",
                    """{"subjectId":"adult","role":"OLDER_ADULT"}""",
                    """{"subjectId":"adult","role":"CAREGIVER","careLinkId":"link"}""",
                )) {
                server.enqueue(MockResponse().setBody(json))
                assertEquals(
                    "INVALID_RESPONSE",
                    (writer.withdraw("adult") as AppResult.Failure).code,
                )
            }
            assertEquals(3, server.requestCount)
            store.value = session()
            assertEquals(
                "RESOURCE_ACCESS_DENIED",
                (writer.withdraw("adult") as AppResult.Failure).code,
            )
            assertEquals(3, server.requestCount)
        }
    }
}
