package com.vitahealth.tata.omission.infrastructure.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.vitahealth.tata.omission.application.PushTopicSubscriptions

/**
 * Follows FCM topics. Firebase starts only when the build includes app/google-services.json; without it
 * nothing is subscribed and the app keeps working without push.
 */
class FirebaseTopicSubscriptions(context: Context) : PushTopicSubscriptions {
    private val appContext = context.applicationContext

    override fun subscribe(topic: String): Boolean {
        if (FirebaseApp.getApps(appContext).isEmpty()) return false
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
        return true
    }
}
