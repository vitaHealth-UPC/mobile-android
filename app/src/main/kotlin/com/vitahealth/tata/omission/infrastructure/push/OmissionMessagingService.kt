package com.vitahealth.tata.omission.infrastructure.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vitahealth.tata.omission.application.DeviceTokenRegistry
import com.vitahealth.tata.omission.domain.model.OmissionPushTopics
import com.vitahealth.tata.omission.presentation.notifications.OmissionNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Receives the omission topics while the app is in the foreground, and data messages in any state.
 * Notification messages that arrive in the background are shown by the system with the backend text.
 */
class OmissionMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenRegistry: DeviceTokenRegistry = PendingDeviceTokenRegistry()

    override fun onMessageReceived(message: RemoteMessage) {
        val body = message.notification?.body ?: message.data["body"]
        val push = OmissionPushTopics.parse(message.from, body) ?: return
        OmissionNotifications.show(this, push)
    }

    override fun onNewToken(token: String) {
        scope.launch { tokenRegistry.register(token) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
