package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.LinkedPersonEmergencyContactUpdater
import com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedLinkedPersonEmergencyContactUpdater
import com.vitahealth.tata.shared.application.session.*
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class UpdateLinkedPersonEmergencyContactTest {
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
    fun validatesCompleteContactBeforeWriting() = runBlocking {
        var writes = 0
        val handler =
            UpdateLinkedPersonEmergencyContactCommandHandler(
                LinkedPersonEmergencyContactUpdater { _, _, _, _, _ ->
                    writes++
                    AppResult.Success(Unit)
                }
            )
        val valid =
            UpdateLinkedPersonEmergencyContactCommand(
                "caregiver",
                "adult",
                "Ana",
                "Hija",
                "+51912345678",
            )
        listOf(
                valid.copy(fullName = ""),
                valid.copy(relationship = ""),
                valid.copy(phone = ""),
                valid.copy(fullName = "x".repeat(161)),
                valid.copy(relationship = "x".repeat(81)),
                valid.copy(phone = "x".repeat(41)),
            )
            .forEach { assertEquals("VALIDATION_ERROR", (handler(it) as AppResult.Failure).code) }
        assertEquals(0, writes)
        assertTrue(handler(valid) is AppResult.Success)
        assertEquals(1, writes)
    }

    @Test
    fun usesCapturedCredentialAndRejectsForeignReceiptAndDeniedWrite() = runBlocking {
        MockWebServer().use { server ->
            val store = Store(session())
            val updater =
                AuthorizedLinkedPersonEmergencyContactUpdater(
                    store,
                    "caregiver",
                    server.url("/").toString(),
                ) {
                    now
                }
            val receipt =
                """{"id":"adult","registeredByCaregiverId":"caregiver","fullName":"Rosa","birthDate":"1958-05-12","emergencyContactName":"Ana","emergencyContactRelationship":"Hija","emergencyContactPhone":"+51912345678"}"""
            server.enqueue(MockResponse().setBody(receipt))
            assertTrue(
                updater.update("caregiver", "adult", "Ana", "Hija", "+51912345678")
                    is AppResult.Success
            )
            val request = server.takeRequest()
            assertEquals("PUT", request.method)
            assertEquals("/api/v1/older-adults/adult/emergency-contact", request.path)
            assertEquals("Bearer captured", request.getHeader("Authorization"))
            assertTrue(request.body.readUtf8().contains("\"relationship\":\"Hija\""))
            server.enqueue(
                MockResponse().setBody(receipt.replace("\"id\":\"adult\"", "\"id\":\"other\""))
            )
            assertEquals(
                "INVALID_RESPONSE",
                (updater.update("caregiver", "adult", "Ana", "Hija", "+51912345678")
                        as AppResult.Failure)
                    .code,
            )
            server.enqueue(MockResponse().setResponseCode(403))
            assertEquals(
                "RESOURCE_ACCESS_DENIED",
                (updater.update("caregiver", "adult", "Ana", "Hija", "+51912345678")
                        as AppResult.Failure)
                    .code,
            )
            store.value = null
            assertEquals(
                "AUTHENTICATION_REQUIRED",
                (updater.update("caregiver", "adult", "Ana", "Hija", "+51912345678")
                        as AppResult.Failure)
                    .code,
            )
            assertEquals(3, server.requestCount)
        }
    }
}
