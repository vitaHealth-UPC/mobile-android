package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.infrastructure.remote.ConfirmIntakeRequest
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.IntakeResponse
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.intake.application.ports.SyncScheduler
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/** Exercises account changes using distinct cache partitions for the same clinical intake. */
class SessionScopedIntakeRepositoriesTest {
    private val ownerA = SessionOwner("account-a", "CAREGIVER")
    private val ownerB = SessionOwner("account-b", "CAREGIVER")
    private val dose =
        DoseDetailReadModel(
            "intake",
            "treatment",
            "medication",
            "adult",
            "Losartán",
            "1",
            "",
            Instant.parse("2099-01-01T12:00:00Z"),
            DoseStatus.PENDING,
        )
    private val partitions = mutableMapOf<SessionOwner, RecordingIntakeLocalStore>()
    private val store = MemorySessionStore()
    private val scheduler =
        object : SyncScheduler {
            override fun schedule() = Unit
        }

    private fun local(owner: SessionOwner) =
        partitions.getOrPut(owner) { RecordingIntakeLocalStore() }

    private fun repositories(remote: (StoredSession) -> IntakeApiService) =
        SessionScopedIntakeRepositories(store, remote, ::local, scheduler)

    private fun login(owner: SessionOwner, token: String = owner.subjectId) =
        store.save(token, "2099-12-31T00:00:00Z", owner)

    @Test
    fun expiredSessionCannotReadOrConfirmUsingRemoteOrCache() = runBlocking {
        val repository = repositories { error("Must not open a repository") }
        store.save("expired", "2000-01-01T00:00:00Z", ownerA)
        assertEquals(
            "SESSION_REQUIRED",
            (repository.getNextDose("adult") as AppResult.Failure).code,
        )
        assertEquals(
            "SESSION_REQUIRED",
            (repository.getDoseDetail("intake") as AppResult.Failure).code,
        )
        assertEquals(
            "SESSION_REQUIRED",
            (repository.getAgenda("adult", Instant.EPOCH, Instant.EPOCH.plusSeconds(60))
                    as AppResult.Failure)
                .code,
        )
        assertEquals(
            "SESSION_REQUIRED",
            (repository.confirm(ConfirmDoseCommand("intake", ConfirmationChannel.TOUCH))
                    as AppResult.Failure)
                .code,
        )
        assertTrue(partitions.isEmpty())
    }

    @Test
    fun adultCannotQueryAnotherAdultsNextDoseOrAgenda() = runBlocking {
        login(SessionOwner("adult-a", "OLDER_ADULT"))
        val repository = repositories { error("Must not open a repository") }
        assertEquals(
            "OLDER_ADULT_SESSION_REQUIRED",
            (repository.getNextDose("adult-b") as AppResult.Failure).code,
        )
        assertEquals(
            "OLDER_ADULT_SESSION_REQUIRED",
            (repository.getAgenda("adult-b", Instant.EPOCH, Instant.EPOCH.plusSeconds(60))
                    as AppResult.Failure)
                .code,
        )
        assertTrue(partitions.isEmpty())
    }

    @Test
    fun resultIsDiscardedWhenSessionExpiresDuringTheRequest() = runBlocking {
        val expiry = Instant.parse("2099-12-31T00:00:00Z")
        var clock = expiry.minusSeconds(1)
        login(ownerA)
        val repository =
            SessionScopedIntakeRepositories(
                store,
                { TestApi(onRead = { clock = expiry }) },
                ::local,
                scheduler,
                { clock },
            )
        assertEquals("SESSION_CHANGED", (repository.getNextDose("adult") as AppResult.Failure).code)
        assertTrue(local(ownerB).cached.isEmpty())
    }

    @Test
    fun cacheAndOfflineConfirmationStayWithTheirOriginalAccount() = runBlocking {
        local(ownerA).cached[dose.id] = dose.copy(medicationName = "Cuenta A")
        local(ownerB).cached[dose.id] = dose.copy(medicationName = "Cuenta B")
        val repository = repositories { TestApi(failure = IOException("offline")) }
        login(ownerA)
        assertEquals(
            "Cuenta A",
            (repository.getNextDose("adult") as AppResult.Success).value?.medicationName,
        )
        assertEquals(
            "PENDING_SYNC",
            (repository.confirm(ConfirmDoseCommand(dose.id, ConfirmationChannel.TOUCH))
                    as AppResult.Failure)
                .code,
        )
        login(ownerB)
        assertEquals(
            "Cuenta B",
            (repository.getNextDose("adult") as AppResult.Success).value?.medicationName,
        )
        assertTrue(local(ownerB).pending.isEmpty())
        assertEquals(1, local(ownerA).pending.size)
        login(ownerA, "renewed-credentials")
        assertEquals(
            "Cuenta A",
            (repository.getNextDose("adult") as AppResult.Success).value?.medicationName,
        )
        assertEquals(1, local(ownerA).pending.size)
    }

    @Test
    fun oldResponseCannotPopulateTheNewAccountsCacheOrReachItsScreen() = runBlocking {
        login(ownerA, "captured-token")
        var capturedToken: String? = null
        val repository = repositories { captured ->
            capturedToken = captured.accessToken
            TestApi(onRead = { login(ownerB, "new-token") })
        }
        assertEquals("SESSION_CHANGED", (repository.getNextDose("adult") as AppResult.Failure).code)
        assertEquals("captured-token", capturedToken)
        assertTrue(local(ownerB).cached.isEmpty())
        assertEquals("intake", local(ownerA).cached[dose.id]?.id)
    }

    @Test
    fun unboundAndConsentOnlySessionsCannotReadAnyPartition() = runBlocking {
        val repository = repositories { error("Must not access remote or cache") }
        store.save("legacy", "2099-12-31T00:00:00Z")
        assertEquals(
            "SESSION_REQUIRED",
            (repository.getNextDose("adult") as AppResult.Failure).code,
        )
        login(SessionOwner("adult", "LINK_SETUP"))
        assertEquals(
            "SESSION_REQUIRED",
            (repository.getDoseDetail("intake") as AppResult.Failure).code,
        )
        store.clear()
        assertEquals(
            "SESSION_REQUIRED",
            (repository.confirm(ConfirmDoseCommand("intake", ConfirmationChannel.TOUCH))
                    as AppResult.Failure)
                .code,
        )
        assertTrue(partitions.isEmpty())
    }

    private class MemorySessionStore : SessionStore {
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

    private class TestApi(
        private val failure: Exception? = null,
        private val onRead: () -> Unit = {},
    ) : IntakeApiService {
        private fun read(): Response<IntakeResponse> {
            onRead()
            failure?.let { throw it }
            return Response.success(
                IntakeResponse(
                    "intake",
                    "treatment",
                    "medication",
                    "adult",
                    "Losartán",
                    "1",
                    "",
                    "2099-01-01T12:00:00Z",
                    "PENDING",
                )
            )
        }

        override suspend fun getNextDose(olderAdultId: String) = read()

        override suspend fun getDoseDetail(intakeId: String) = read()

        override suspend fun confirmDose(intakeId: String, request: ConfirmIntakeRequest) = read()

        override suspend fun getAgenda(
            olderAdultId: String,
            from: String,
            to: String,
        ): Response<List<IntakeResponse>> = error("Unused")
    }
}
