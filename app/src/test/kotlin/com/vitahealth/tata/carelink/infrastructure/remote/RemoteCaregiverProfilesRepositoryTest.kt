package com.vitahealth.tata.carelink.infrastructure.remote

import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteCaregiverProfilesRepositoryTest {
    @Test
    fun duplicateAdultRowsAreRejectedInsteadOfShowingAmbiguousProfiles() = runBlocking {
        val repository =
            RemoteCaregiverProfilesRepository(
                api { _, _ ->
                    Response.success(
                        listOf(
                            ConfirmedLinkResponse("link-1", "adult-1", "Rosa"),
                            ConfirmedLinkResponse("link-2", "adult-1", "Another name"),
                        )
                    )
                }
            )
        assertEquals("INVALID_RESPONSE", (repository.list("care-1") as AppResult.Failure).code)
    }

    @Test
    fun `invalid linking codes cannot be handed to the consent screen`() = runBlocking {
        val valid =
            CareLinkResponse(
                "link",
                "care-1",
                "adult-1",
                "PENDING",
                "ABC123",
                "2099-01-01T00:00:00Z",
                null,
                false,
                null,
                null,
            )
        for (body in
            listOf(
                valid.copy(caregiverId = "other-caregiver"),
                valid.copy(olderAdultId = "other-adult"),
                valid.copy(linkingCode = ""),
                valid.copy(codeExpiresAt = "invalid"),
                valid.copy(codeExpiresAt = "2000-01-01T00:00:00Z"),
                valid.copy(codeExpiresAt = null),
                valid.copy(id = ""),
                valid.copy(status = "CONFIRMED"),
                valid.copy(status = "REVOKED"),
                valid.copy(codeUsedAt = "2026-10-26T12:00:00Z"),
                valid.copy(confirmedAt = "2026-10-26T12:00:00Z"),
                valid.copy(consentGranted = true),
                valid.copy(consentRecordedAt = "2026-10-26T12:00:00Z"),
            )) {
            val repository =
                RemoteCaregiverProfilesRepository(api { _, _ -> Response.success(body) })
            val result = repository.linkingCode("care-1", LinkedAdult("adult-1", "Rosa"))
            assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
        }
    }

    @Test
    fun `registration for another caregiver is rejected`() = runBlocking {
        val body =
            OlderAdultProfileResponse(
                "adult-1",
                "other-caregiver",
                "Rosa",
                "1958-05-12",
                null,
                null,
                null,
                null,
            )
        val repository = RemoteCaregiverProfilesRepository(api { _, _ -> Response.success(body) })

        val result =
            repository.register(
                "care-1",
                NewAdultProfile("Rosa", LocalDate.of(1958, 5, 12), "", "", ""),
            )

        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    @Test
    fun `blank adult identities are rejected`() = runBlocking {
        val repository =
            RemoteCaregiverProfilesRepository(
                api { _, _ -> Response.success(listOf(ConfirmedLinkResponse("link", "", "Rosa"))) }
            )

        val result = repository.list("care-1")

        assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
    }

    private fun api(call: (String, Array<out Any>) -> Any?): CaregiverProfilesApiService =
        java.lang.reflect.Proxy.newProxyInstance(
            CaregiverProfilesApiService::class.java.classLoader,
            arrayOf(CaregiverProfilesApiService::class.java),
        ) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as CaregiverProfilesApiService

    @Test
    fun `confirmed links map to adult identities rather than link ids`() = runBlocking {
        val repository =
            RemoteCaregiverProfilesRepository(
                api { _, args ->
                    assertEquals("care-1", args[0])
                    Response.success(listOf(ConfirmedLinkResponse("link-1", "adult-1", "Rosa")))
                }
            )
        val result = repository.list("care-1") as AppResult.Success
        assertEquals(LinkedAdult("adult-1", "Rosa"), result.value.single())
    }

    @Test
    fun `profile registration sends date and optional emergency contact`() = runBlocking {
        val repository =
            RemoteCaregiverProfilesRepository(
                api { _, args ->
                    val body = args[0] as RegisterAdultRequest
                    assertEquals("1958-05-12", body.birthDate)
                    assertNull(body.emergencyContactPhone)
                    Response.success(
                        OlderAdultProfileResponse(
                            "adult-1",
                            "care-1",
                            body.fullName,
                            body.birthDate,
                            null,
                            null,
                            null,
                            null,
                        )
                    )
                }
            )
        val result =
            repository.register(
                "care-1",
                NewAdultProfile(" Rosa ", LocalDate.of(1958, 5, 12), "", "", ""),
            ) as AppResult.Success
        assertEquals("Rosa", result.value.name)
    }

    @Test
    fun `linking code preserves identity expiration and code for consent handoff`() = runBlocking {
        val repository =
            RemoteCaregiverProfilesRepository(
                api { _, args ->
                    assertEquals("adult-1", (args[0] as GenerateCodeRequest).olderAdultId)
                    Response.success(
                        CareLinkResponse(
                            "link-1",
                            "care-1",
                            "adult-1",
                            "PENDING",
                            "ABC123",
                            "2099-01-01T00:00:00Z",
                            null,
                            false,
                            null,
                            null,
                        )
                    )
                }
            )
        val result =
            repository.linkingCode("care-1", LinkedAdult("adult-1", "Rosa")) as AppResult.Success
        assertEquals("ABC123", result.value.code)
        assertEquals("adult-1", result.value.olderAdultId)
    }
}
