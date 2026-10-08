package com.vitahealth.tata.preferences.domain.model

/**
 * Daily interval in which non-critical notifications are held back.
 * It may cross midnight, for example 22:00 to 07:00.
 */
data class QuietHours(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
) {
    init {
        require(startHour in 0..23 && endHour in 0..23) { "hour must be between 0 and 23" }
        require(startMinute in 0..59 && endMinute in 0..59) { "minute must be between 0 and 59" }
        require(startHour != endHour || startMinute != endMinute) {
            "quiet hours must start and end at different times"
        }
    }

    companion object {
        /** Suggested interval the first time the user turns quiet hours on. */
        val Default = QuietHours(startHour = 22, startMinute = 0, endHour = 7, endMinute = 0)
    }
}
