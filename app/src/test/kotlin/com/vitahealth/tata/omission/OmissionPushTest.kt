package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommand
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommandHandler
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommand
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommandHandler
import com.vitahealth.tata.omission.application.policies.OmissionPushTopics
import com.vitahealth.tata.omission.application.ports.PushTargetStore
import com.vitahealth.tata.omission.application.queries.ResolvePushDestinationQueryHandler
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
import com.vitahealth.tata.omission.presentation.notifications.mappers.OmissionPushIntent
import com.vitahealth.tata.shared.common.result.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** US-22 push reception, with the recipients and texts of the backend PushNotificationAdapter. */
class OmissionPushTest {

    private class FakeStore : PushTargetStore {
        val caregivers = mutableMapOf<String, String>()
        val adults = mutableMapOf<String, String>()

        override fun rememberCaregiver(olderAdultId: String, caregiverId: String) {
            caregivers[olderAdultId] = caregiverId
        }

        override fun rememberOlderAdult(olderAdultId: String, olderAdultName: String) {
            adults[olderAdultId] = olderAdultName
        }

        override fun caregiverFor(olderAdultId: String) = caregivers[olderAdultId]

        override fun olderAdultName(olderAdultId: String) = adults[olderAdultId]
    }

    private val store = FakeStore()

    @Test
    fun topicsUseTheBackendRecipients() {
        assertEquals("user-adult-1", OmissionPushTopics.forOlderAdult("adult-1"))
        assertEquals("caregivers-of-adult-1", OmissionPushTopics.forCaregiversOf(" adult-1 "))
    }

    @Test
    fun parsesTheReinforcedReminder() {
        val push =
            OmissionPushTopics.parse(
                from = "/topics/user-adult-1",
                body = "Your Losartan 50 mg is still pending. Please confirm it when you take it.",
            )

        assertEquals(
            OmissionPush(OmissionPushKind.REINFORCED_REMINDER, "adult-1", "Losartan 50 mg"),
            push,
        )
    }

    @Test
    fun parsesTheCaregiverAlert() {
        val push =
            OmissionPushTopics.parse(
                from = "/topics/caregivers-of-adult-1",
                body = "Losartan 50 mg was not confirmed. Please check how they are doing.",
            )

        assertEquals(
            OmissionPush(OmissionPushKind.CAREGIVER_ALERT, "adult-1", "Losartan 50 mg"),
            push,
        )
    }

    @Test
    fun anotherBodyKeepsTheKindWithoutMedication() {
        val push = OmissionPushTopics.parse(from = "/topics/user-adult-1", body = "Recordatorio")

        assertEquals(OmissionPushKind.REINFORCED_REMINDER, push!!.kind)
        assertNull(push.medicationName)
    }

    @Test
    fun ignoresMessagesThatAreNotOmissionTopics() {
        assertNull(OmissionPushTopics.parse(from = "1234567890", body = "x"))
        assertNull(OmissionPushTopics.parse(from = "/topics/news", body = "x"))
        assertNull(OmissionPushTopics.parse(from = "/topics/user-", body = "x"))
        assertNull(OmissionPushTopics.parse(from = null, body = "x"))
    }

    @Test
    fun caregiverFollowsTheAlertsOfTheOlderAdult() {
        val result =
            FollowCaregiverAlertsCommandHandler(store)(
                FollowCaregiverAlertsCommand("caregiver-1", "adult-1")
            )

        assertEquals(Unit, (result as AppResult.Success).value)
        assertEquals("caregiver-1", store.caregiverFor("adult-1"))
    }

    @Test
    fun olderAdultFollowsTheirReminders() {
        FollowDoseRemindersCommandHandler(store)(
            FollowDoseRemindersCommand("adult-1", "Rosa Vargas")
        )

        assertEquals("Rosa Vargas", store.olderAdultName("adult-1"))
    }

    @Test
    fun rememberingATargetDoesNotRequireOrClaimPushTransport() {
        val handler = FollowCaregiverAlertsCommandHandler(store)

        val result = handler(FollowCaregiverAlertsCommand("caregiver-1", "adult-1"))

        assertEquals(Unit, (result as AppResult.Success).value)
        assertEquals("caregiver-1", store.caregiverFor("adult-1"))
    }

    @Test
    fun blankReferencesAreRejected() {
        val result =
            FollowCaregiverAlertsCommandHandler(store)(FollowCaregiverAlertsCommand(" ", "adult-1"))

        assertEquals(OmissionFailureCodes.INVALID_REFERENCE, (result as AppResult.Failure).code)
    }

    @Test
    fun aTappedAlertOpensTheAlertsOfTheFollowedOlderAdult() {
        store.rememberCaregiver("adult-1", "caregiver-1")
        val query = OmissionPushIntent.queryFrom("CAREGIVER_ALERT", "adult-1")!!

        assertEquals(
            PushDestination.CaregiverAlerts("caregiver-1", "adult-1"),
            ResolvePushDestinationQueryHandler(store)(query),
        )
    }

    @Test
    fun aTappedReminderOpensTheHomeOfTheOlderAdult() {
        store.rememberOlderAdult("adult-1", "Rosa Vargas")
        val query = OmissionPushIntent.queryFrom("REINFORCED_REMINDER", "adult-1")!!

        assertEquals(
            PushDestination.OlderAdultHome("adult-1", "Rosa Vargas"),
            ResolvePushDestinationQueryHandler(store)(query),
        )
    }

    @Test
    fun aVersionedReminderOpensItsExactIntakeWithoutNeedingARememberedName() {
        val query =
            OmissionPushIntent.queryFrom(
                "REINFORCED_REMINDER",
                "adult-1",
                intakeId = " intake-7 ",
            )!!
        assertEquals(
            PushDestination.OlderAdultDose("adult-1", "intake-7"),
            ResolvePushDestinationQueryHandler(store)(query),
        )
        assertNull(OmissionPushIntent.queryFrom("REINFORCED_REMINDER", "adult-1", intakeId = " "))
    }

    @Test
    fun aVersionedCaregiverNotificationRetainsTheIntakeReference() {
        store.rememberCaregiver("adult-1", "caregiver-1")
        val query =
            OmissionPushIntent.queryFrom("CAREGIVER_ALERT", "adult-1", intakeId = "intake-7")!!
        assertEquals(
            PushDestination.CaregiverAlerts("caregiver-1", "adult-1", intakeId = "intake-7"),
            ResolvePushDestinationQueryHandler(store)(query),
        )
    }

    @Test
    fun unknownTargetsAndIntentsOpenNothing() {
        assertNull(
            ResolvePushDestinationQueryHandler(store)(
                OmissionPushIntent.queryFrom("CAREGIVER_ALERT", "adult-9")!!
            )
        )
        assertNull(OmissionPushIntent.queryFrom(null, "adult-1"))
        assertNull(OmissionPushIntent.queryFrom("SOMETHING_ELSE", "adult-1"))
        assertNull(OmissionPushIntent.queryFrom("CAREGIVER_ALERT", " "))
    }
}
