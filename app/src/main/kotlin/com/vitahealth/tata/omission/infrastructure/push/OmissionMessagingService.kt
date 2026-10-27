package com.vitahealth.tata.omission.infrastructure.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vitahealth.tata.omission.application.policies.OmissionPushTopics
import com.vitahealth.tata.omission.application.ports.OmissionPushReceiver
import com.vitahealth.tata.omission.application.ports.PushTokenChangeReceiver

/**
 * Maps versioned direct data messages and legacy topic messages to the application's recipient
 * boundary. Platform notification messages received in the background remain managed by Android;
 * the current backend uses data messages so local session checks apply before display.
 */
class OmissionMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val body = message.notification?.body ?: message.data["body"]
        val push =
            if (message.data.containsKey("schemaVersion")) message.data.toOmissionPush()
            else OmissionPushTopics.parse(message.from, body)
        if (push != null) (application as OmissionPushReceiver).onOmissionPushReceived(push)
    }

    override fun onNewToken(token: String) {
        (application as PushTokenChangeReceiver).onPushTokenChanged(token)
    }
}
