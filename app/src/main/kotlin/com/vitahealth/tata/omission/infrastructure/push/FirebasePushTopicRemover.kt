package com.vitahealth.tata.omission.infrastructure.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.vitahealth.tata.omission.application.ports.PushTopicRemover
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Awaits Firebase's acknowledgment; a queued request is not treated as successful retirement. */
class FirebasePushTopicRemover(context: Context) : PushTopicRemover {
    private val appContext = context.applicationContext

    override suspend fun remove(topic: String): AppResult<Unit> {
        if (FirebaseApp.getApps(appContext).isEmpty())
            return AppResult.Failure("Push is not configured", code = "PUSH_NOT_CONFIGURED")
        return try {
            suspendCancellableCoroutine { continuation ->
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener {
                    task ->
                    if (continuation.isActive)
                        continuation.resume(
                            if (task.isSuccessful) AppResult.Success(Unit)
                            else
                                AppResult.Failure(
                                    "Topic retirement failed",
                                    code = "PUSH_RETIREMENT_FAILED",
                                )
                        )
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: RuntimeException) {
            AppResult.Failure("Topic retirement failed", code = "PUSH_RETIREMENT_FAILED")
        }
    }
}
