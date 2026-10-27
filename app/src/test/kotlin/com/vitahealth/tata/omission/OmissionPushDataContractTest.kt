package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.policies.OmissionPushAccessPolicy
import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import com.vitahealth.tata.omission.infrastructure.push.toOmissionPush
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks the backend data-message schema and local recipient isolation independently of FCM. */
class OmissionPushDataContractTest {
    private val payload =
        mapOf(
            "schemaVersion" to "1",
            "deliveryId" to "delivery",
            "intakeId" to "intake",
            "olderAdultId" to "adult",
            "kind" to "REINFORCED_REMINDER",
            "medicationName" to "Losartán",
            "scheduledAt" to "2026-10-23T12:00:00Z",
        )

    @Test
    fun directDataMessagePreservesDeliveryAndClinicalReferences() {
        val push = requireNotNull(payload.toOmissionPush())
        assertEquals("delivery", push.deliveryId)
        assertEquals("intake", push.intakeId)
        assertEquals("adult", push.olderAdultId)
        assertEquals(Instant.parse("2026-10-23T12:00:00Z"), push.scheduledAt)
        assertEquals(OmissionPushKind.REINFORCED_REMINDER, push.kind)
        assertNotNull((payload + ("kind" to "CAREGIVER_ALERT")).toOmissionPush())
    }

    @Test
    fun incompleteOrUnknownDataCannotFallBackToAnInventedNotification() {
        for (key in payload.keys) {
            assertNull(key, (payload - key).toOmissionPush())
            assertNull(key, (payload + (key to " ")).toOmissionPush())
        }
        assertNull((payload + ("schemaVersion" to "2")).toOmissionPush())
        assertNull((payload + ("kind" to "UNKNOWN")).toOmissionPush())
        assertNull((payload + ("scheduledAt" to "invalid")).toOmissionPush())
    }

    @Test
    fun delayedMessagesRequireTheCurrentRecipientSession() {
        var current: StoredSession? = null
        val sessions =
            object : SessionStore {
                override fun session() = current

                override fun accessToken() = current?.accessToken

                override fun save(accessToken: String, expiresAt: String) = error("Unused")

                override fun save(accessToken: String, expiresAt: String, owner: SessionOwner) =
                    error("Unused")

                override fun clear() {
                    current = null
                }
            }
        val targets =
            object : PushTargetStore {
                override fun caregiverFor(olderAdultId: String) =
                    "caregiver-a".takeIf { olderAdultId == "adult" }

                override fun olderAdultName(olderAdultId: String): String? = null

                override fun rememberCaregiver(olderAdultId: String, caregiverId: String) = Unit

                override fun rememberOlderAdult(olderAdultId: String, olderAdultName: String) = Unit
            }
        val policy = OmissionPushAccessPolicy(sessions, targets)
        val reminder = requireNotNull(payload.toOmissionPush())
        assertFalse(policy.allows(reminder))
        current = StoredSession("token", Instant.MAX, SessionOwner("adult", "OLDER_ADULT"))
        assertTrue(policy.allows(reminder))
        val doseDestination = PushDestination.OlderAdultDose("adult", "intake")
        assertTrue(policy.allowsDestination(doseDestination))
        current = StoredSession("token", Instant.MAX, SessionOwner("other", "OLDER_ADULT"))
        assertFalse(policy.allows(reminder))
        assertFalse(policy.allowsDestination(doseDestination))
        val alert = reminder.copy(kind = OmissionPushKind.CAREGIVER_ALERT)
        current = StoredSession("token", Instant.MAX, SessionOwner("caregiver-a", "CAREGIVER"))
        assertTrue(policy.allows(alert))
        assertTrue(
            policy.allowsDestination(PushDestination.CaregiverAlerts("caregiver-a", "adult"))
        )
        assertFalse(
            policy.allowsDestination(PushDestination.CaregiverAlerts("caregiver-b", "adult"))
        )
        current = StoredSession("token", Instant.MAX, SessionOwner("caregiver-b", "CAREGIVER"))
        assertFalse(policy.allows(alert))
        current = StoredSession("token", Instant.EPOCH, SessionOwner("caregiver-a", "CAREGIVER"))
        assertFalse(policy.allows(alert))
    }
}
