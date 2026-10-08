package com.vitahealth.tata.omission.domain.model

/** The two notifications the backend Omission & Escalation context sends (TS-05 / TS-06). */
enum class OmissionPushKind {
    /** Reinforced reminder to the older adult while the intake is still pending (US-22). */
    REINFORCED_REMINDER,

    /** Alert to the caregivers once the intake is registered as omitted. */
    CAREGIVER_ALERT,
}

data class OmissionPush(
    val kind: OmissionPushKind,
    val olderAdultId: String,
    val medicationName: String?,
)

/**
 * Recipients of the backend `PushProviderMessage`: `user-<olderAdultId>` for the reminder and
 * `caregivers-of-<olderAdultId>` for the caregiver alert. The device follows them as FCM topics,
 * which FCM reports in the message `from` as `/topics/<name>`.
 */
object OmissionPushTopics {
    private const val TOPIC_PREFIX = "/topics/"
    private const val OLDER_ADULT_PREFIX = "user-"
    private const val CAREGIVERS_PREFIX = "caregivers-of-"

    fun forOlderAdult(olderAdultId: String): String = OLDER_ADULT_PREFIX + olderAdultId.trim()

    fun forCaregiversOf(olderAdultId: String): String = CAREGIVERS_PREFIX + olderAdultId.trim()

    /** Returns null for messages that do not come from an omission topic. */
    fun parse(from: String?, body: String?): OmissionPush? {
        val topic = from?.removePrefix(TOPIC_PREFIX) ?: return null
        return when {
            topic.startsWith(CAREGIVERS_PREFIX) -> topic.removePrefix(CAREGIVERS_PREFIX).takeIf { it.isNotBlank() }
                ?.let { OmissionPush(OmissionPushKind.CAREGIVER_ALERT, it, caregiverAlertMedication(body)) }
            topic.startsWith(OLDER_ADULT_PREFIX) -> topic.removePrefix(OLDER_ADULT_PREFIX).takeIf { it.isNotBlank() }
                ?.let { OmissionPush(OmissionPushKind.REINFORCED_REMINDER, it, reminderMedication(body)) }
            else -> null
        }
    }

    // Bodies written by the backend PushNotificationAdapter; the medication is read back so the device
    // can show the notification in the user's language.
    private val reminderBody = Regex("^Your (.+) is still pending\\. Please confirm it when you take it\\.$")
    private val caregiverBody = Regex("^(.+) was not confirmed\\. Please check how they are doing\\.$")

    private fun reminderMedication(body: String?): String? = body?.trim()?.let { reminderBody.find(it)?.groupValues?.get(1) }

    private fun caregiverAlertMedication(body: String?): String? = body?.trim()?.let { caregiverBody.find(it)?.groupValues?.get(1) }
}
