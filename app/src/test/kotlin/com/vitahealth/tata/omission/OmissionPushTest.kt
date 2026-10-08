package com.vitahealth.tata.omission

import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.omission.application.PushTargetStore
import com.vitahealth.tata.omission.application.PushTopicSubscriptions
import com.vitahealth.tata.omission.application.commands.FollowCaregiverAlertsCommand
import com.vitahealth.tata.omission.application.commands.FollowDoseRemindersCommand
import com.vitahealth.tata.omission.application.handlers.FollowCaregiverAlertsCommandHandler
import com.vitahealth.tata.omission.application.handlers.FollowDoseRemindersCommandHandler
import com.vitahealth.tata.omission.application.handlers.ResolvePushDestinationQueryHandler
import com.vitahealth.tata.omission.application.queries.PushDestination
import com.vitahealth.tata.omission.domain.model.OmissionPush
import com.vitahealth.tata.omission.domain.model.OmissionPushKind
import com.vitahealth.tata.omission.domain.model.OmissionPushTopics
import com.vitahealth.tata.omission.infrastructure.push.PendingDeviceTokenRegistry
import com.vitahealth.tata.omission.presentation.notifications.OmissionPushIntent
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** US-22 push reception, with the recipients and texts of the backend PushNotificationAdapter. */
class OmissionPushTest {

    private class FakeStore : PushTargetStore {
        val caregivers = mutableMapOf<String, String>()
        val adults = mutableMapOf<String, String>()
        override fun rememberCaregiver(olderAdultId: String, caregiverId: String) { caregivers[olderAdultId] = caregiverId }
        override fun rememberOlderAdult(olderAdultId: String, olderAdultName: String) { adults[olderAdultId] = olderAdultName }
        override fun caregiverFor(olderAdultId: String) = caregivers[olderAdultId]
        override fun olderAdultName(olderAdultId: String) = adults[olderAdultId]
    }

    private class FakeSubscriptions(private val configured: Boolean = true) : PushTopicSubscriptions {
        val topics = mutableListOf<String>()
        override fun subscribe(topic: String): Boolean {
            topics += topic
            return configured
        }
    }

    private val store = FakeStore()
    private val subscriptions = FakeSubscriptions()

    @Test fun topicsUseTheBackendRecipients() {
        assertEquals("user-adult-1", OmissionPushTopics.forOlderAdult("adult-1"))
        assertEquals("caregivers-of-adult-1", OmissionPushTopics.forCaregiversOf(" adult-1 "))
    }

    @Test fun parsesTheReinforcedReminder() {
        val push = OmissionPushTopics.parse(
            from = "/topics/user-adult-1",
            body = "Your Losartan 50 mg is still pending. Please confirm it when you take it.",
        )

        assertEquals(OmissionPush(OmissionPushKind.REINFORCED_REMINDER, "adult-1", "Losartan 50 mg"), push)
    }

    @Test fun parsesTheCaregiverAlert() {
        val push = OmissionPushTopics.parse(
            from = "/topics/caregivers-of-adult-1",
            body = "Losartan 50 mg was not confirmed. Please check how they are doing.",
        )

        assertEquals(OmissionPush(OmissionPushKind.CAREGIVER_ALERT, "adult-1", "Losartan 50 mg"), push)
    }

    @Test fun anotherBodyKeepsTheKindWithoutMedication() {
        val push = OmissionPushTopics.parse(from = "/topics/user-adult-1", body = "Recordatorio")

        assertEquals(OmissionPushKind.REINFORCED_REMINDER, push!!.kind)
        assertNull(push.medicationName)
    }

    @Test fun ignoresMessagesThatAreNotOmissionTopics() {
        assertNull(OmissionPushTopics.parse(from = "1234567890", body = "x"))
        assertNull(OmissionPushTopics.parse(from = "/topics/news", body = "x"))
        assertNull(OmissionPushTopics.parse(from = "/topics/user-", body = "x"))
        assertNull(OmissionPushTopics.parse(from = null, body = "x"))
    }

    @Test fun caregiverFollowsTheAlertsOfTheOlderAdult() {
        val result = FollowCaregiverAlertsCommandHandler(store, subscriptions)(FollowCaregiverAlertsCommand("caregiver-1", "adult-1"))

        assertTrue((result as AppResult.Success).value)
        assertEquals(listOf("caregivers-of-adult-1"), subscriptions.topics)
        assertEquals("caregiver-1", store.caregiverFor("adult-1"))
    }

    @Test fun olderAdultFollowsTheirReminders() {
        FollowDoseRemindersCommandHandler(store, subscriptions)(FollowDoseRemindersCommand("adult-1", "Rosa Vargas"))

        assertEquals(listOf("user-adult-1"), subscriptions.topics)
        assertEquals("Rosa Vargas", store.olderAdultName("adult-1"))
    }

    @Test fun withoutFirebaseTheTargetIsStillRemembered() {
        val handler = FollowCaregiverAlertsCommandHandler(store, FakeSubscriptions(configured = false))

        val result = handler(FollowCaregiverAlertsCommand("caregiver-1", "adult-1"))

        assertFalse((result as AppResult.Success).value)
        assertEquals("caregiver-1", store.caregiverFor("adult-1"))
    }

    @Test fun blankReferencesAreRejected() {
        val result = FollowCaregiverAlertsCommandHandler(store, subscriptions)(FollowCaregiverAlertsCommand(" ", "adult-1"))

        assertEquals(OmissionFailureCodes.INVALID_REFERENCE, (result as AppResult.Failure).code)
        assertTrue(subscriptions.topics.isEmpty())
    }

    @Test fun aTappedAlertOpensTheAlertsOfTheFollowedOlderAdult() {
        store.rememberCaregiver("adult-1", "caregiver-1")
        val query = OmissionPushIntent.queryFrom("CAREGIVER_ALERT", "adult-1")!!

        assertEquals(PushDestination.CaregiverAlerts("caregiver-1", "adult-1"), ResolvePushDestinationQueryHandler(store)(query))
    }

    @Test fun aTappedReminderOpensTheHomeOfTheOlderAdult() {
        store.rememberOlderAdult("adult-1", "Rosa Vargas")
        val query = OmissionPushIntent.queryFrom("REINFORCED_REMINDER", "adult-1")!!

        assertEquals(PushDestination.OlderAdultHome("adult-1", "Rosa Vargas"), ResolvePushDestinationQueryHandler(store)(query))
    }

    @Test fun unknownTargetsAndIntentsOpenNothing() {
        assertNull(ResolvePushDestinationQueryHandler(store)(OmissionPushIntent.queryFrom("CAREGIVER_ALERT", "adult-9")!!))
        assertNull(OmissionPushIntent.queryFrom(null, "adult-1"))
        assertNull(OmissionPushIntent.queryFrom("SOMETHING_ELSE", "adult-1"))
        assertNull(OmissionPushIntent.queryFrom("CAREGIVER_ALERT", " "))
    }

    @Test fun deviceTokenRegistrationIsPendingUntilTheBackendHasAnEndpoint() = runBlocking {
        val result = PendingDeviceTokenRegistry().register("token")

        assertEquals(OmissionFailureCodes.DEVICE_TOKEN_ENDPOINT_MISSING, (result as AppResult.Failure).code)
    }
}
