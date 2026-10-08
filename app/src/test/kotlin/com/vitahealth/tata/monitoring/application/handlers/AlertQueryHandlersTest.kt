package com.vitahealth.tata.monitoring.application.handlers

import com.vitahealth.tata.monitoring.FakeAlertsRepository
import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.queries.GetAlertDetailQuery
import com.vitahealth.tata.monitoring.application.queries.GetOpenAlertsQuery
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertQueryHandlersTest {
    private val repository = FakeAlertsRepository()

    @Test fun listsPendingAlertsFirstAndTheMostRecentFirst() = runBlocking {
        repository.list = AppResult.Success(listOf(
            alert(id = 1, status = AlertStatus.ATTENDED, scheduledAt = "2026-10-06T13:00:00Z"),
            alert(id = 2, status = AlertStatus.OPEN, scheduledAt = "2026-10-04T13:00:00Z"),
            alert(id = 3, status = AlertStatus.OPEN, scheduledAt = "2026-10-05T13:00:00Z"),
        ))

        val result = GetOpenAlertsQueryHandler(repository)(GetOpenAlertsQuery(" caregiver ", "adult"))

        assertEquals(listOf(3L, 2L, 1L), (result as AppResult.Success).value.map { it.id })
        assertEquals("list:caregiver:adult", repository.requests.single())
    }

    @Test fun blankReferencesNeverReachTheRepository() = runBlocking {
        val list = GetOpenAlertsQueryHandler(repository)(GetOpenAlertsQuery("", "adult"))
        val detail = GetAlertDetailQueryHandler(repository)(GetAlertDetailQuery("caregiver", "adult", 0))

        assertEquals(AlertFailureCodes.INVALID_REFERENCE, (list as AppResult.Failure).code)
        assertEquals(AlertFailureCodes.INVALID_REFERENCE, (detail as AppResult.Failure).code)
        assertTrue(repository.requests.isEmpty())
    }

    @Test fun readsTheRequestedAlert() = runBlocking {
        repository.detail = AppResult.Success(alert(id = 7))

        val result = GetAlertDetailQueryHandler(repository)(GetAlertDetailQuery("caregiver", "adult", 7))

        assertEquals(7L, (result as AppResult.Success).value.id)
        assertEquals("detail:caregiver:adult:7", repository.requests.single())
    }
}
