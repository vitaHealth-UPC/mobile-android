package com.vitahealth.tata.monitoring.presentation.summary

import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.repositories.ContactRepository
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryViewModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FamilySummaryViewModelTest {
    private val summaries = mutableListOf<Continuation<AppResult<FamilySummary>>>()
    private val histories = mutableListOf<Continuation<AppResult<List<IntakeHistoryEntry>>>>()
    private val contacts = mutableListOf<Continuation<AppResult<ContactChannel>>>()
    private val repository =
        object : FamilyMonitoringRepository {
            override suspend fun summary(
                caregiverId: String,
                olderAdultId: String,
                name: String,
                day: LocalDate,
                zone: ZoneId,
            ): AppResult<FamilySummary> = suspendCoroutine { summaries += it }

            override suspend fun history(
                caregiverId: String,
                olderAdultId: String,
            ): AppResult<List<IntakeHistoryEntry>> = suspendCoroutine { histories += it }
        }
    private val contactRepository =
        object : ContactRepository {
            override suspend fun contactChannel(
                caregiverId: String,
                olderAdultId: String,
            ): AppResult<ContactChannel> = suspendCoroutine { contacts += it }

            override suspend fun olderAdultFullName(olderAdultId: String): AppResult<String> =
                AppResult.Success("Ana")
        }

    private fun model() =
        FamilySummaryViewModel(
            "caregiver",
            "adult",
            "Ana",
            GetFamilySummaryQueryHandler(repository),
            GetFamilyHistoryQueryHandler(repository),
            GetContactOptionQueryHandler(contactRepository),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun olderRefreshCannotReplaceTheNewerResult() {
        val model = model()
        model.refresh()
        model.refresh()
        summaries[1].resume(AppResult.Failure("Latest result"))
        summaries[0].resume(AppResult.Failure("Stale result"))
        assertEquals("Latest result", model.state.value.error)
    }

    @Test
    fun dismissedHistoryDoesNotReopenAfterLateResponse() {
        val model = model()
        model.history()
        model.dismissDialog()
        histories.single().resume(AppResult.Success(emptyList()))
        assertNull(model.state.value.dialog)
    }

    @Test
    fun lateContactCannotReplaceTheNewHistoryDialog() {
        val model = model()
        model.contact()
        model.history()
        histories.single().resume(AppResult.Success(emptyList()))
        contacts.single().resume(AppResult.Failure("Late contact"))
        assertEquals("Historial de los últimos 7 días", model.state.value.dialog?.title)
        assertEquals(emptyList<String>(), model.state.value.dialog?.rows)
    }

    @Test
    fun lostAccessFromContactClearsSummaryAndCancelsLateRefresh() {
        listOf("SESSION_REQUIRED", "SESSION_CHANGED", "CARE_RELATIONSHIP_REQUIRED").forEach { code
            ->
            val model = model()
            model.refresh()
            summaries.last().resume(AppResult.Success(summary()))
            model.refresh()
            val pendingRefresh = summaries.last()
            model.contact()
            contacts.last().resume(AppResult.Failure("Access lost", code = code))
            assertNull(model.state.value.summary)
            assertNull(model.state.value.dialog)
            assertEquals(code, model.state.value.errorCode)
            pendingRefresh.resume(AppResult.Success(summary()))
            assertNull(model.state.value.summary)
            assertEquals(code, model.state.value.errorCode)
        }
    }

    @Test
    fun lostAccessFromHistoryClearsSummaryAndDialog() {
        listOf("SESSION_REQUIRED", "SESSION_CHANGED", "CARE_RELATIONSHIP_REQUIRED").forEach { code
            ->
            val model = model()
            model.refresh()
            summaries.last().resume(AppResult.Success(summary()))
            model.history()
            histories.last().resume(AppResult.Failure("Access lost", code = code))
            assertNull(model.state.value.summary)
            assertNull(model.state.value.dialog)
            assertEquals(code, model.state.value.errorCode)
        }
    }

    @Test
    fun lostAccessFromRefreshCancelsLateHistory() {
        val model = model()
        model.history()
        model.refresh()
        summaries.last().resume(AppResult.Failure("Access lost", code = "SESSION_CHANGED"))
        histories.last().resume(AppResult.Success(emptyList()))
        assertNull(model.state.value.summary)
        assertNull(model.state.value.dialog)
        assertEquals("SESSION_CHANGED", model.state.value.errorCode)
    }

    private fun summary() =
        FamilySummary(
            "Ana",
            LocalDate.of(2026, 10, 24),
            AdherenceCounts(2, 3),
            AdherenceCounts(1, 1),
            null,
            emptyList(),
            emptyList(),
            true,
        )
}
