package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.ports.VoiceConfirmationRepository
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationStatus
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

/** Checks voice session binding without interpreting JWT or opaque token contents. */
class SessionScopedVoiceConfirmationRepositoryTest {
    private val store = Store()
    private val owner = SessionOwner("adult", "OLDER_ADULT")
    private val command = ConfirmDoseByVoiceCommand("intake", byteArrayOf(1), "audio/mp4", "es-419")
    private val outcome =
        AppResult.Success(VoiceConfirmationReadModel(VoiceConfirmationStatus.NOT_RECOGNIZED))

    private fun remote(call: suspend () -> AppResult<VoiceConfirmationReadModel>) =
        object : VoiceConfirmationRepository {
            override suspend fun confirm(command: ConfirmDoseByVoiceCommand) = call()
        }

    private fun login(token: String = "opaque-token") =
        store.save(token, "2099-01-01T00:00:00Z", owner)

    @Test
    fun capturesCredentialsAndPreservesTheServerOutcome() = runBlocking {
        login()
        val repository =
            SessionScopedVoiceConfirmationRepository(store) { captured ->
                assertEquals("opaque-token", captured.accessToken)
                assertEquals(owner, captured.owner)
                remote { outcome }
            }
        assertSame(outcome, repository.confirm(command))
    }

    @Test
    fun changedSessionNeverPublishesAnOldAccountsResult() = runBlocking {
        for (logout in listOf(false, true)) {
            login()
            val repository =
                SessionScopedVoiceConfirmationRepository(store) {
                    remote {
                        if (logout) store.clear()
                        else
                            store.save(
                                "another-token",
                                "2099-01-01T00:00:00Z",
                                SessionOwner("another", "CAREGIVER"),
                            )
                        outcome
                    }
                }
            assertEquals("SESSION_CHANGED", (repository.confirm(command) as AppResult.Failure).code)
        }
    }

    @Test
    fun unboundMissingAndSetupSessionsNeverSendAudio() = runBlocking {
        val repository = SessionScopedVoiceConfirmationRepository(store) { error("Must not send") }
        assertEquals("SESSION_REQUIRED", (repository.confirm(command) as AppResult.Failure).code)
        store.save("legacy", "2099-01-01T00:00:00Z")
        assertEquals("SESSION_REQUIRED", (repository.confirm(command) as AppResult.Failure).code)
        store.save("setup", "2099-01-01T00:00:00Z", SessionOwner("adult", "LINK_SETUP"))
        assertEquals("SESSION_REQUIRED", (repository.confirm(command) as AppResult.Failure).code)
    }

    @Test
    fun cancellationReachesTheCaller() = runBlocking {
        login()
        val expected = CancellationException("left screen")
        val repository =
            SessionScopedVoiceConfirmationRepository(store) { remote { throw expected } }
        try {
            repository.confirm(command)
            fail("Expected cancellation")
        } catch (actual: CancellationException) {
            assertSame(expected, actual)
        }
    }

    private class Store : SessionStore {
        private var current: StoredSession? = null

        override fun save(accessToken: String, expiresAt: String) {
            current = StoredSession(accessToken, Instant.parse(expiresAt), null)
        }

        override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
            current = StoredSession(accessToken, Instant.parse(expiresAt), owner)
        }

        override fun session() = current

        override fun accessToken() = current?.accessToken

        override fun clear() {
            current = null
        }
    }
}
