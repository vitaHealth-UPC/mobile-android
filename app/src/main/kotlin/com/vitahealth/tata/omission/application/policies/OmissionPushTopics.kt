package com.vitahealth.tata.omission.application.policies

import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush
import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPushKind
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
        val topic =
            from?.takeIf { it.startsWith(TOPIC_PREFIX) }
                ?.removePrefix(TOPIC_PREFIX)
                ?: return null
        return when {
            topic.startsWith(CAREGIVERS_PREFIX) ->
                topic
                    .removePrefix(CAREGIVERS_PREFIX)
                    .takeIf { it.isNotBlank() && it.none { char -> char.isWhitespace() || char == '/' } }
                    ?.let {
                        OmissionPush(
                            OmissionPushKind.CAREGIVER_ALERT,
                            it,
                            caregiverAlertMedication(body),
                        )
                    }
            topic.startsWith(OLDER_ADULT_PREFIX) ->
                topic
                    .removePrefix(OLDER_ADULT_PREFIX)
                    .takeIf { it.isNotBlank() && it.none { char -> char.isWhitespace() || char == '/' } }
                    ?.let {
                        OmissionPush(
                            OmissionPushKind.REINFORCED_REMINDER,
                            it,
                            reminderMedication(body),
                        )
                    }
            else -> null
        }
    }

    /**
     * Extracts the medication from legacy English messages, so the device can localize the notice.
     * The backend owns the original message contract and clinical omission state.
     */
    private val reminderBody =
        Regex("^Your (.+) is still pending\\. Please confirm it when you take it\\.$")
    private val caregiverBody =
        Regex("^(.+) was not confirmed\\. Please check how they are doing\\.$")

    private fun reminderMedication(body: String?): String? =
        body?.trim()?.let { reminderBody.find(it)?.groupValues?.get(1) }

    private fun caregiverAlertMedication(body: String?): String? =
        body?.trim()?.let { caregiverBody.find(it)?.groupValues?.get(1) }
}
