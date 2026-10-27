package com.vitahealth.tata.preferences.domain.model.valueobjects

/** Device-applied accessibility values, independent of a particular storage mechanism. */
data class AccessibilityPreferences(
    val textSize: TextSizeLevel = TextSizeLevel.MEDIUM,
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val readingAssistance: Boolean = false,
    val voiceConfirmation: Boolean = true,
    val darkTheme: Boolean = false,
) {
    /**
     * "Large text" is the single switch shown to the user; every size from LARGE up counts as on.
     */
    val largeTextEnabled: Boolean
        get() = textSize.ordinal >= TextSizeLevel.LARGE.ordinal

    companion object {
        val Defaults = AccessibilityPreferences()
    }
}
