package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.policies.OmissionPushTopics
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** Prevents malformed push evidence and unintended legacy-topic matches. */
class OmissionPushDomainBoundaryTest {

    @Test
    fun evidenceAlwaysReferencesAnOlderAdultIncludingAfterCopy() {
        val push = OmissionPush(OmissionPushKind.CAREGIVER_ALERT, "adult-1", "Losartán")

        assertThrows(IllegalArgumentException::class.java) {
            push.copy(olderAdultId = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            push.copy(intakeId = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            push.copy(deliveryId = " ")
        }
        assertEquals("adult-1", push.olderAdultId)
    }

    @Test
    fun legacyTopicParsingRequiresTheRealFcmTopicPrefix() {
        assertNull(
            OmissionPushTopics.parse(
                from = "user-adult-1",
                body = "Your Losartan is still pending. Please confirm it when you take it.",
            )
        )
        assertNull(
            OmissionPushTopics.parse(
                from = "/topics/caregivers-of-adult-1/untrusted",
                body = "Losartan was not confirmed. Please check how they are doing.",
            )
        )
        assertNull(OmissionPushTopics.parse(from = "/topics/user- ", body = "x"))
        assertEquals(
            "adult-1",
            OmissionPushTopics.parse(from = "/topics/user-adult-1", body = "x")?.olderAdultId,
        )
    }
}
