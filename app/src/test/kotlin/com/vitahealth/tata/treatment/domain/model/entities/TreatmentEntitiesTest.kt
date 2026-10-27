package com.vitahealth.tata.treatment.domain.model.entities

import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Protects identity and owner invariants of the client-side domain entities. */
class TreatmentEntitiesTest {

    @Test
    fun treatmentCannotBeCreatedOrCopiedWithMissingOwnership() {
        val treatment = Treatment("t-1", "adult-1", "Hipertension", TreatmentStatus.DRAFT)

        assertThrows(IllegalArgumentException::class.java) {
            treatment.copy(olderAdultId = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            treatment.copy(id = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            treatment.copy(name = " ")
        }
        assertEquals(TreatmentStatus.DRAFT, treatment.status)
    }

    @Test
    fun medicationRejectsBlankIdentityAndPresentationEvenOnCopy() {
        val medication = Medication("m-1", "adult-1", "Losartan", "Tableta", true)

        assertThrows(IllegalArgumentException::class.java) {
            medication.copy(olderAdultId = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            medication.copy(presentation = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Medication(" ", "adult-1", "Losartan", "Tableta", true)
        }
        assertEquals("Tableta", medication.presentation)
    }
}
