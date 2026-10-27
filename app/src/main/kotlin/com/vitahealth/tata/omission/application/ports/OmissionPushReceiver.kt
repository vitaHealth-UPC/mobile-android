package com.vitahealth.tata.omission.application.ports

import com.vitahealth.tata.omission.domain.model.valueobjects.OmissionPush

/**
 * Hands incoming messages to the process composition for session checks and notification display.
 */
fun interface OmissionPushReceiver {
    fun onOmissionPushReceived(push: OmissionPush)
}
