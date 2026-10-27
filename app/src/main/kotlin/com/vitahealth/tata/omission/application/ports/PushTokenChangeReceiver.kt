package com.vitahealth.tata.omission.application.ports

/**
 * Receives SDK token rotation without exposing application composition to the messaging adapter.
 */
fun interface PushTokenChangeReceiver {
    fun onPushTokenChanged(token: String)
}
