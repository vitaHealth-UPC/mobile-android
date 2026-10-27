package com.vitahealth.tata.monitoring.domain.model

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.NoteTextProblem
import com.vitahealth.tata.monitoring.domain.model.valueobjects.canMoveTo
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protects existing domain invariants without duplicating backend authorization. */
class MonitoringDomainRulesTest {

    @Test
    fun closedAlertsRequireConsistentTimestampsEvenAfterCopy() {
        val opened = Instant.parse("2026-10-24T08:00:00Z")
        val alert = CaregiverAlert(
            id = 10L,
            intakeId = "intake-1",
            medicationName = "Medicamento",
            scheduledAt = opened,
            reason = "Dose not taken",
            status = AlertStatus.OPEN,
            openedAt = opened,
            closedAt = null,
        )

        assertThrows(IllegalArgumentException::class.java) {
            alert.copy(id = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            alert.copy(status = AlertStatus.CLOSED)
        }
        assertThrows(IllegalArgumentException::class.java) {
            alert.copy(closedAt = opened)
        }
        assertEquals(AlertStatus.OPEN, alert.status)
    }

    @Test
    fun alertTransitionsFollowBackendSupportedDirections() {
        assertTrue(AlertStatus.OPEN.canMoveTo(AlertStatus.ATTENDED))
        assertTrue(AlertStatus.ATTENDED.canMoveTo(AlertStatus.CLOSED))
        assertFalse(AlertStatus.CLOSED.canMoveTo(AlertStatus.OPEN))
        assertFalse(AlertStatus.ATTENDED.canMoveTo(AlertStatus.OPEN))
    }

    @Test
    fun countsAndNoteTextMustRemainCoherent() {
        assertThrows(IllegalArgumentException::class.java) {
            AdherenceCounts(confirmed = 4, total = 3)
        }
        assertEquals(null, AdherenceCounts(0, 0).percentage)
        assertEquals(50.0, AdherenceCounts(1, 2).percentage!!, 0.001)
        assertEquals(NoteTextProblem.BLANK, FollowUpNote.problemWith(" "))
        assertEquals(NoteTextProblem.TOO_LONG, FollowUpNote.problemWith("a".repeat(1001)))
    }
}
