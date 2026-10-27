package com.vitahealth.tata.treatment.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsOverviewReader
import com.vitahealth.tata.treatment.application.ports.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import com.vitahealth.tata.treatment.application.readmodels.MyMedicationsOverview
import com.vitahealth.tata.treatment.application.queries.GetMyMedicationsOverviewQueryHandler
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionScopedMyMedicationsOverviewReaderTest {
    private var current: StoredSession? = null
    private val sessions =
        object : SessionStore {
            override fun session() = current

            override fun accessToken() = current?.accessToken

            override fun clear() {
                current = null
            }

            override fun save(accessToken: String, expiresAt: String) {
                current = StoredSession(accessToken, Instant.parse(expiresAt), null)
            }

            override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) {
                current = StoredSession(accessToken, Instant.parse(expiresAt), owner)
            }
        }

    @Test
    fun missingExpiredOrMismatchedSessionDoesNotOpenSources() = runBlocking {
        var calls = 0
        val reader =
            SessionScopedMyMedicationsOverviewReader(sessions, "adult") {
                calls++
                MyMedicationsOverviewReader {
                    AppResult.Success(MyMedicationsOverview(emptyList(), null))
                }
            }
        assertEquals("SESSION_REQUIRED", (reader.read() as AppResult.Failure).code)
        sessions.save("token", "2000-01-01T00:00:00Z", SessionOwner("adult", "OLDER_ADULT"))
        assertEquals("SESSION_REQUIRED", (reader.read() as AppResult.Failure).code)
        for (owner in
            listOf(SessionOwner("other", "OLDER_ADULT"), SessionOwner("adult", "CAREGIVER"))) {
            sessions.save("token", "2099-01-01T00:00:00Z", owner)
            assertEquals("OLDER_ADULT_SESSION_REQUIRED", (reader.read() as AppResult.Failure).code)
        }
        assertEquals(0, calls)
    }

    @Test
    fun responseIsDiscardedAfterSessionChanges() = runBlocking {
        sessions.save("original", "2099-01-01T00:00:00Z", SessionOwner("adult", "OLDER_ADULT"))
        val reader =
            SessionScopedMyMedicationsOverviewReader(sessions, "adult") { captured ->
                assertEquals("original", captured.accessToken)
                MyMedicationsOverviewReader {
                    sessions.save(
                        "new",
                        "2099-01-01T00:00:00Z",
                        SessionOwner("other", "OLDER_ADULT"),
                    )
                    AppResult.Success(MyMedicationsOverview(emptyList(), null))
                }
            }
        assertEquals("SESSION_CHANGED", (reader.read() as AppResult.Failure).code)
    }

    @Test
    fun bothReadsUseCapturedSessionWhenAccountChangesBetweenThem() = runBlocking {
        sessions.save("original", "2099-01-01T00:00:00Z", SessionOwner("adult", "OLDER_ADULT"))
        var nextDoseCalls = 0
        val reader =
            SessionScopedMyMedicationsOverviewReader(sessions, "adult") { captured ->
                val catalog =
                    object : MyMedicationsRepository {
                        override suspend fun list(): AppResult<List<MyMedication>> {
                            assertEquals("original", captured.accessToken)
                            sessions.clear()
                            return AppResult.Success(emptyList())
                        }
                    }
                GetMyMedicationsOverviewQueryHandler(catalog) {
                    nextDoseCalls++
                    assertEquals("original", captured.accessToken)
                    AppResult.Success(null)
                }
            }
        assertEquals("SESSION_CHANGED", (reader.read() as AppResult.Failure).code)
        assertEquals(1, nextDoseCalls)
    }
}
