package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.Assert.*
import org.junit.Test

class RemotePasswordRecoveryTest {
    @Test
    fun requestAndCompletionUseAnonymousExactContractsWithoutNormalizingSecrets() = runBlocking {
        MockWebServer().use { server ->
            val remote =
                RemotePasswordRecovery(
                    ApiClientFactory.create(server.url("/").toString()) { null }
                        .create(PasswordRecoveryApi::class.java)
                )
            server.enqueue(MockResponse().setResponseCode(202))
            assertTrue(remote.request(" user@example.com ") is AppResult.Success)
            val request = server.takeRequest()
            assertEquals("/api/v1/password-recovery-requests", request.path)
            assertNull(request.getHeader("Authorization"))
            assertEquals("{\"email\":\"user@example.com\"}", request.body.readUtf8())
            server.enqueue(MockResponse().setResponseCode(204))
            assertTrue(
                remote.complete("user@example.com", "012345", " new-password ") is AppResult.Success
            )
            val completion = server.takeRequest()
            assertEquals("/api/v1/password-recoveries", completion.path)
            assertTrue(completion.body.readUtf8().contains("\"newPassword\":\" new-password \""))
        }
    }

    @Test
    fun rejectsUnexpectedSuccessAndPreservesRecoveryError() = runBlocking {
        MockWebServer().use { server ->
            val remote =
                RemotePasswordRecovery(
                    ApiClientFactory.create(server.url("/").toString()) { null }
                        .create(PasswordRecoveryApi::class.java)
                )
            server.enqueue(MockResponse().setResponseCode(200))
            assertEquals(
                "INVALID_RESPONSE",
                (remote.request("user@example.com") as AppResult.Failure).code,
            )
            server.enqueue(
                MockResponse().setResponseCode(400).setBody("""{"title":"INVALID_RECOVERY_CODE"}""")
            )
            assertEquals(
                "INVALID_RECOVERY_CODE",
                (remote.complete("user@example.com", "012345", "new-password") as AppResult.Failure)
                    .code,
            )
            assertFalse(
                CompletePasswordRecoveryBody("private-email", "private-code", "private-password")
                    .toString()
                    .contains("private")
            )
        }
    }
}
