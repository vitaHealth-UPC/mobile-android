package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.AlertTimelineReader
import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.readmodels.AlertTimeline
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.domain.repositories.AlertsRepository
import com.vitahealth.tata.monitoring.domain.repositories.ContactRepository
import com.vitahealth.tata.monitoring.domain.repositories.NotesRepository
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SessionScopedFamilyMonitoringRepositoryTest {
    private var current: StoredSession? = null
    private val sessions =
        object : SessionStore {
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
    private val result = AppResult.Failure("server failure", code = "REQUEST_FAILED")

    private fun remote(onCall: () -> Unit = {}) =
        object : FamilyMonitoringRepository {
            override suspend fun summary(
                caregiverId: String,
                olderAdultId: String,
                name: String,
                day: LocalDate,
                zone: ZoneId,
            ): AppResult<FamilySummary> {
                onCall()
                return result
            }

            override suspend fun history(
                caregiverId: String,
                olderAdultId: String,
            ): AppResult<List<IntakeHistoryEntry>> {
                onCall()
                return result
            }
        }

    private fun login(id: String = "caregiver", role: String = "CAREGIVER") =
        sessions.save("token-$id", "2099-01-01T00:00:00Z", SessionOwner(id, role))

    @Test
    fun contactLookupsShareCapturedCredentialsAndDiscardResultAfterAccountSwitch() = runBlocking {
        login()
        var factories = 0
        var nameCalls = 0
        val reader =
            SessionScopedContactOptionReader(sessions) { captured ->
                factories++
                assertEquals("token-caregiver", captured.accessToken)
                object : ContactRepository {
                    override suspend fun contactChannel(
                        caregiverId: String,
                        olderAdultId: String,
                    ): AppResult<ContactChannel> {
                        login("other")
                        return AppResult.Success(
                            ContactChannel(ContactChannelType.PHONE, "+51999999999")
                        )
                    }

                    override suspend fun olderAdultFullName(
                        olderAdultId: String
                    ): AppResult<String> {
                        nameCalls++
                        assertEquals("token-caregiver", captured.accessToken)
                        return AppResult.Success("Rosa Vargas")
                    }
                }
            }
        assertEquals(
            "SESSION_CHANGED",
            (reader(GetContactOptionQuery("caregiver", "adult")) as AppResult.Failure).code,
        )
        assertEquals(1, factories)
        assertEquals(1, nameCalls)
        assertEquals(
            "CARE_RELATIONSHIP_REQUIRED",
            (reader(GetContactOptionQuery("caregiver", "adult")) as AppResult.Failure).code,
        )
        assertEquals(1, factories)
    }

    @Test
    fun personalNotesRequireAdultAndDiscardResponsesAfterLogout() = runBlocking {
        val remote =
            object : PersonalNotesRepository {
                override suspend fun list(): AppResult<List<PersonalNote>> {
                    sessions.clear()
                    return result
                }

                override suspend fun create(
                    title: String,
                    text: String,
                    category: PersonalNoteCategory,
                ): AppResult<PersonalNote> {
                    sessions.clear()
                    return result
                }
            }
        val scoped =
            SessionScopedPersonalNotesRepository(sessions) { captured ->
                assertEquals("token-adult", captured.accessToken)
                remote
            }
        login()
        assertEquals("OLDER_ADULT_SESSION_REQUIRED", (scoped.list() as AppResult.Failure).code)
        for (create in listOf(false, true)) {
            login("adult", "OLDER_ADULT")
            val response =
                if (create) scoped.create("Nota", "Contenido", PersonalNoteCategory.ROUTINE)
                else scoped.list()
            assertEquals("SESSION_CHANGED", (response as AppResult.Failure).code)
        }
    }

    @Test
    fun allAlertAndNoteOperationsRejectResultsAfterLogout() = runBlocking {
        val alerts =
            object : AlertsRepository {
                override suspend fun openAlerts(
                    caregiverId: String,
                    olderAdultId: String,
                ): AppResult<List<CaregiverAlert>> {
                    sessions.clear()
                    return result
                }

                override suspend fun alert(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                ): AppResult<CaregiverAlert> {
                    sessions.clear()
                    return result
                }

                override suspend fun updateStatus(
                    caregiverId: String,
                    olderAdultId: String,
                    alertId: Long,
                    status: AlertStatus,
                ): AppResult<CaregiverAlert> {
                    sessions.clear()
                    return result
                }
            }
        val notes =
            object : NotesRepository {
                override suspend fun notes(
                    caregiverId: String,
                    olderAdultId: String,
                ): AppResult<List<FollowUpNote>> {
                    sessions.clear()
                    return result
                }

                override suspend fun register(
                    caregiverId: String,
                    olderAdultId: String,
                    text: String,
                ): AppResult<FollowUpNote> {
                    sessions.clear()
                    return result
                }
            }
        val scopedAlerts =
            SessionScopedAlertsRepository(sessions) { captured ->
                assertEquals("token-caregiver", captured.accessToken)
                alerts
            }
        val scopedNotes =
            SessionScopedNotesRepository(sessions) { captured ->
                assertEquals("token-caregiver", captured.accessToken)
                notes
            }
        for (operation in 0..4) {
            login()
            val response =
                when (operation) {
                    0 -> scopedAlerts.openAlerts("caregiver", "adult")
                    1 -> scopedAlerts.alert("caregiver", "adult", 1)
                    2 -> scopedAlerts.updateStatus("caregiver", "adult", 1, AlertStatus.ATTENDED)
                    3 -> scopedNotes.notes("caregiver", "adult")
                    else -> scopedNotes.register("caregiver", "adult", "Llamada realizada")
                }
            assertEquals("SESSION_CHANGED", (response as AppResult.Failure).code)
        }
    }

    @Test
    fun wrongOrAbsentCaregiverNeverStartsProtectedReads() = runBlocking {
        val repository =
            SessionScopedFamilyMonitoringRepository(sessions) { error("No remote expected") }
        assertEquals(
            "SESSION_REQUIRED",
            (repository.history("caregiver", "adult") as AppResult.Failure).code,
        )
        login("other")
        assertEquals(
            "CARE_RELATIONSHIP_REQUIRED",
            (repository.history("caregiver", "adult") as AppResult.Failure).code,
        )
        login("caregiver", "OLDER_ADULT")
        assertEquals(
            "CARE_RELATIONSHIP_REQUIRED",
            (repository.history("caregiver", "adult") as AppResult.Failure).code,
        )
    }

    @Test
    fun fixedCredentialsAndOriginalResultArePreserved() = runBlocking {
        login()
        val repository =
            SessionScopedFamilyMonitoringRepository(sessions) { captured ->
                assertEquals("token-caregiver", captured.accessToken)
                remote()
            }
        assertSame(result, repository.history("caregiver", "adult"))
    }

    @Test
    fun summaryAndHistoryRejectResponsesAfterLogout() = runBlocking {
        for (history in listOf(true, false)) {
            login()
            val repository =
                SessionScopedFamilyMonitoringRepository(sessions) { remote { sessions.clear() } }
            val actual =
                if (history) repository.history("caregiver", "adult")
                else
                    repository.summary(
                        "caregiver",
                        "adult",
                        "Ana",
                        LocalDate.of(2026, 10, 6),
                        ZoneId.of("America/Bogota"),
                    )
            assertEquals("SESSION_CHANGED", (actual as AppResult.Failure).code)
        }
    }

    @Test
    fun timelineDiscardsAResultAfterAccountSwitchAndRejectsWrongOwnerBeforeCallingRemote() =
        runBlocking {
            login()
            var calls = 0
            val reader =
                SessionScopedAlertTimelineReader(sessions) { captured ->
                    assertEquals("token-caregiver", captured.accessToken)
                    AlertTimelineReader { _, _, day, _ ->
                        calls++
                        login("other")
                        AppResult.Success(AlertTimeline(emptyList(), emptyList(), "Rosa", day))
                    }
                }
            val changed = reader.read("caregiver", "adult", LocalDate.now(), ZoneId.of("UTC"))
            assertEquals("SESSION_CHANGED", (changed as AppResult.Failure).code)
            val rejected = reader.read("caregiver", "adult", LocalDate.now(), ZoneId.of("UTC"))
            assertEquals("CARE_RELATIONSHIP_REQUIRED", (rejected as AppResult.Failure).code)
            assertEquals(1, calls)
        }
}
