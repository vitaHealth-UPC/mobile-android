package com.vitahealth.tata.inventory.infrastructure.security

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.ports.InventoryRepository
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks credentials and account changes around inventory reads and both mutations. */
class SessionScopedInventoryRepositoryTest {
    private val owner = SessionOwner("caregiver-a", "CAREGIVER")
    private val other = SessionOwner("caregiver-b", "CAREGIVER")
    private val expiry = Instant.parse("2099-01-01T00:00:00Z")
    private val sessions = MemorySessions()
    private val source = FakeInventoryRepository()

    @Test
    fun invalidSessionsNeverOpenTheRemoteRepository() = runTest {
        val repository =
            SessionScopedInventoryRepository(sessions, owner, { error("Must not open remote") })
        val invalid =
            listOf(
                null,
                StoredSession("legacy", expiry, null),
                StoredSession("expired", Instant.EPOCH, owner),
                StoredSession("setup", expiry, SessionOwner("adult", "LINK_SETUP")),
            )
        for (session in invalid) {
            sessions.current = session
            assertCodes(repository, "SESSION_REQUIRED")
        }
    }

    @Test
    fun oldScreenCannotReadOrMutateWithAnotherAccountsCredentials() = runTest {
        sessions.current = StoredSession("new-account", expiry, other)
        val repository =
            SessionScopedInventoryRepository(sessions, owner, { error("Must not open remote") })
        assertCodes(repository, "SESSION_CHANGED")
    }

    @Test
    fun sessionChangeDuringEachOperationDiscardsItsResponse() = runTest {
        val capturedTokens = mutableListOf<String>()
        val repository =
            SessionScopedInventoryRepository(
                sessions,
                owner,
                { captured ->
                    capturedTokens += captured.accessToken
                    sessions.current = StoredSession("other-account", expiry, other)
                    source
                },
            )
        val operations: List<suspend () -> AppResult<InventoryStockReadModel>> =
            listOf(
                { repository.getStock("med-1") },
                { repository.registerInitialInventory("med-1", 30, 5) },
                { repository.registerReplenishment("med-1", 30, "lot") },
            )
        for (operation in operations) {
            sessions.current = StoredSession("original-token", expiry, owner)
            assertEquals("SESSION_CHANGED", (operation() as AppResult.Failure).code)
        }
        assertEquals(listOf("original-token", "original-token", "original-token"), capturedTokens)
    }

    @Test
    fun credentialRenewalForSameAccountIsUsedByTheNextOperation() = runTest {
        val tokens = mutableListOf<String>()
        val repository =
            SessionScopedInventoryRepository(
                sessions,
                owner,
                {
                    tokens += it.accessToken
                    source
                },
            )
        sessions.current = StoredSession("first", expiry, owner)
        assertTrue(repository.getStock("med-1") is AppResult.Success)
        sessions.current = StoredSession("renewed", expiry, owner)
        assertTrue(repository.registerReplenishment("med-1", 30, "lot") is AppResult.Success)
        assertEquals(listOf("first", "renewed"), tokens)
    }

    @Test
    fun expirationDuringReadDiscardsTheResult() = runTest {
        var now = expiry.minusSeconds(1)
        sessions.current = StoredSession("token", expiry, owner)
        val remote =
            object : InventoryRepository by source {
                override suspend fun getStock(
                    medicationId: String
                ): AppResult<InventoryStockReadModel> {
                    now = expiry
                    return source.getStock(medicationId)
                }
            }
        val repository = SessionScopedInventoryRepository(sessions, owner, { remote }, { now })
        assertEquals("SESSION_CHANGED", (repository.getStock("med-1") as AppResult.Failure).code)
    }

    private suspend fun assertCodes(repository: InventoryRepository, code: String) {
        assertEquals(code, (repository.getStock("med-1") as AppResult.Failure).code)
        assertEquals(
            code,
            (repository.registerInitialInventory("med-1", 30, 5) as AppResult.Failure).code,
        )
        assertEquals(
            code,
            (repository.registerReplenishment("med-1", 30, "lot") as AppResult.Failure).code,
        )
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
