package com.vitahealth.tata.carelink.domain.model.entities

import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Protects entity identity while leaving consent transitions on the server. */
class CareLinkEntityInvariantsTest {

    @Test
    fun linkRequiresRealReferencesEvenWhenCopied() {
        val link =
            CareLink(
                id = "link-1",
                caregiverId = "caregiver-1",
                olderAdultId = "adult-1",
                status = CareLinkStatus.AWAITING_CONSENT,
                codeExpiresAt = null,
                codeUsedAt = null,
                consentGranted = false,
                consentRecordedAt = null,
                confirmedAt = null,
            )

        assertThrows(IllegalArgumentException::class.java) { link.copy(id = " ") }
        assertThrows(IllegalArgumentException::class.java) { link.copy(caregiverId = "") }
        assertThrows(IllegalArgumentException::class.java) { link.copy(olderAdultId = " ") }
        assertEquals(CareLinkStatus.AWAITING_CONSENT, link.status)
    }

    @Test
    fun olderAdultRejectsMissingIdentityOrName() {
        val adult =
            OlderAdultProfile(
                id = "adult-1",
                fullName = "Rosa Vargas",
                birthDate = LocalDate.of(1958, 5, 12),
                emergencyContactName = null,
                emergencyContactRelationship = null,
                emergencyContactPhone = null,
            )
        assertThrows(IllegalArgumentException::class.java) { adult.copy(id = "") }
        assertThrows(IllegalArgumentException::class.java) { adult.copy(fullName = " ") }
        assertEquals("Rosa Vargas", adult.fullName)
    }
}
