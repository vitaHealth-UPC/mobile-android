package com.vitahealth.tata.omission.infrastructure.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.vitahealth.tata.omission.application.ports.DevicePushTokenSource
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Reads Firebase's persisted token without keeping a second plaintext token store. */
class FirebaseDevicePushTokenSource(context: Context) : DevicePushTokenSource {
    private val appContext = context.applicationContext

    override suspend fun currentToken(): AppResult<String?> {
        if (FirebaseApp.getApps(appContext).isEmpty()) return AppResult.Success(null)
        return try {
            suspendCancellableCoroutine { continuation ->
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (continuation.isActive)
                        continuation.resume(
                            if (task.isSuccessful) AppResult.Success(task.result)
                            else
                                AppResult.Failure(
                                    "No se pudo obtener el token push.",
                                    code = "PUSH_TOKEN_UNAVAILABLE",
                                )
                        )
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: RuntimeException) {
            AppResult.Failure("No se pudo obtener el token push.", code = "PUSH_TOKEN_UNAVAILABLE")
        }
    }
}
