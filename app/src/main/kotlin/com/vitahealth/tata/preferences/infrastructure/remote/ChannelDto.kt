package com.vitahealth.tata.preferences.infrastructure.remote

/** Notification channel payload; missing enabled values remain detectable. */
data class ChannelDto(val type: String, val enabled: Boolean?)
