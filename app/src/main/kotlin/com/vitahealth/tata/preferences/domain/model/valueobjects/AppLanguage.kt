package com.vitahealth.tata.preferences.domain.model.valueobjects

/** The two localized experiences provided by this application. */
enum class AppLanguage(val tag: String, val displayName: String) {
    SPANISH("es-419", "Español"),
    ENGLISH("en", "English");

    fun next(): AppLanguage = if (this == SPANISH) ENGLISH else SPANISH
    companion object {
        fun fromTag(value: String?) = entries.firstOrNull { it.tag == value } ?: SPANISH
    }
}
