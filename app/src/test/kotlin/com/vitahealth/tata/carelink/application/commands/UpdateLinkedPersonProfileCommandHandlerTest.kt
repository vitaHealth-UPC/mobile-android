package com.vitahealth.tata.carelink.application.commands

import com.vitahealth.tata.carelink.application.ports.LinkedPersonProfileUpdater
import com.vitahealth.tata.carelink.infrastructure.remote.AuthorizedLinkedPersonProfileUpdater
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class UpdateLinkedPersonProfileCommandHandlerTest {
    private val date = LocalDate.parse("1958-05-12")

    @Test fun rejectsInvalidFieldsBeforeWriting() = runBlocking {
        var writes = 0
        val handler = UpdateLinkedPersonProfileCommandHandler(
            LinkedPersonProfileUpdater { _, _, _, _ -> writes++; AppResult.Success(Unit) }
        )
        listOf(
            UpdateLinkedPersonProfileCommand("", "adult", "Rosa", date),
            UpdateLinkedPersonProfileCommand("caregiver", "", "Rosa", date),
            UpdateLinkedPersonProfileCommand("caregiver", "adult", "", date),
            UpdateLinkedPersonProfileCommand("caregiver", "adult", "X".repeat(161), date),
            UpdateLinkedPersonProfileCommand("caregiver", "adult", "Rosa", LocalDate.now().plusDays(1))
        ).forEach {
            assertEquals("VALIDATION_ERROR", (handler(it) as AppResult.Failure).code)
        }
        assertEquals(0, writes)
    }

    @Test fun trimsAndDelegatesTheAuthorizedSelection() = runBlocking {
        val writes = mutableListOf<String>()
        val handler = UpdateLinkedPersonProfileCommandHandler(
            LinkedPersonProfileUpdater { caregiver, adult, name, birth ->
                writes.add(listOf(caregiver, adult, name, birth.toString()).joinToString("|"))
                AppResult.Success(Unit)
            }
        )
        assertTrue(handler(UpdateLinkedPersonProfileCommand(
            " caregiver ", " adult ", "  Rosa Vargas ", date
        )) is AppResult.Success)
        assertEquals(listOf("caregiver|adult|Rosa Vargas|1958-05-12"), writes)
    }

    @Test fun absentExpiredOrForeignSessionCannotWrite() = runBlocking {
        val now = Instant.parse("2026-10-26T12:00:00Z")
        val cases = listOf(
            null,
            StoredSession("token", now, SessionOwner("caregiver", "CAREGIVER")),
            StoredSession("token", now.plusSeconds(300), SessionOwner("another", "CAREGIVER")),
            StoredSession("token", now.plusSeconds(300), SessionOwner("caregiver", "OLDER_ADULT"))
        )
        cases.forEach { session ->
            val store = object : SessionStore {
                override fun save(accessToken: String, expiresAt: String) = Unit
                override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) = Unit
                override fun session(): StoredSession? = session
                override fun accessToken(): String? = session?.accessToken
                override fun clear() = Unit
            }
            val writer = AuthorizedLinkedPersonProfileUpdater(
                store, "caregiver", "http://localhost:1/", now = { now }
            )
            assertEquals(
                if (session == null || !session.expiresAt.isAfter(now))
                    "AUTHENTICATION_REQUIRED" else "RESOURCE_ACCESS_DENIED",
                (writer.update("caregiver", "adult", "Rosa", date)
                    as AppResult.Failure).code
            )
        }
    }
}
