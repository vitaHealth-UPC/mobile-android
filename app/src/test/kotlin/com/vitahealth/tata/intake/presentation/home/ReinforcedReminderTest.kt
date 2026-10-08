package com.vitahealth.tata.intake.presentation.home

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** US-22: the second reminder shows only while the scheduled dose is still pending. */
class ReinforcedReminderTest {
    private val scheduledAt = Instant.parse("2026-10-05T13:00:00Z")

    private fun dose(status: DoseStatus) = NextDoseReadModel(
        id = "101", treatmentId = "t", medicationId = "m", olderAdultId = "adult-1",
        medicationName = "Losartan 50 mg", dose = "1 tablet", instructions = "",
        scheduledAt = scheduledAt, status = status,
    )

    @Test fun notBeforeTheScheduledTime() {
        assertFalse(isReinforcedReminderDue(dose(DoseStatus.PENDING), scheduledAt.minusSeconds(60)))
    }

    @Test fun fromTheScheduledTimeWhileStillPending() {
        assertTrue(isReinforcedReminderDue(dose(DoseStatus.PENDING), scheduledAt))
        assertTrue(isReinforcedReminderDue(dose(DoseStatus.PENDING), scheduledAt.plusSeconds(25 * 60)))
    }

    @Test fun neverOnceTheDoseIsResolved() {
        listOf(DoseStatus.CONFIRMED, DoseStatus.LATE, DoseStatus.OMITTED).forEach {
            assertFalse(it.name, isReinforcedReminderDue(dose(it), scheduledAt.plusSeconds(60)))
        }
    }
}
