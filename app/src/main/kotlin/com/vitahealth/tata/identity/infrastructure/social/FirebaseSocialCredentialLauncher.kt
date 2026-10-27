package com.vitahealth.tata.identity.infrastructure.social

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.vitahealth.tata.BuildConfig
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.presentation.access.SocialCredentialLauncher
import com.vitahealth.tata.identity.presentation.access.SocialProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Exchanges Google/Facebook provider credentials through Firebase Auth; never persists raw tokens. */
class FirebaseSocialCredentialLauncher(private val context: Context) : SocialCredentialLauncher {
    private val callbackManager by lazy { CallbackManager.Factory.create() }

    override fun isAvailable(provider: SocialProvider): Boolean {
        if (FirebaseApp.getApps(context).isEmpty()) return false
        return when (provider) {
            SocialProvider.GOOGLE ->
                BuildConfig.GOOGLE_LOGIN_CONFIGURED && googleWebClientId() != null
            SocialProvider.FACEBOOK -> BuildConfig.FACEBOOK_LOGIN_CONFIGURED
        }
    }

    private fun googleWebClientId(): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id).takeIf { it.isNotBlank() }
    }

    override suspend fun request(provider: SocialProvider, activity: ComponentActivity): String {
        if (!isAvailable(provider)) throw IllegalStateException("SOCIAL_UNAVAILABLE")
        val auth = FirebaseAuth.getInstance()
        val credential = when (provider) {
            SocialProvider.GOOGLE -> {
                val option = GetSignInWithGoogleOption.Builder(googleWebClientId()!!).build()
                val result = CredentialManager.create(activity).getCredential(
                    activity,
                    GetCredentialRequest.Builder().addCredentialOption(option).build(),
                )
                val custom = result.credential as? CustomCredential
                    ?: throw IllegalStateException("INVALID_SOCIAL_CREDENTIAL")
                if (custom.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                    throw IllegalStateException("INVALID_SOCIAL_CREDENTIAL")
                val idToken = GoogleIdTokenCredential.createFrom(custom.data).idToken
                GoogleAuthProvider.getCredential(idToken, null)
            }
            SocialProvider.FACEBOOK -> {
                val facebookToken = facebookAccessToken(activity)
                FacebookAuthProvider.getCredential(facebookToken)
            }
        }
        // The Tata server verifies its own Firebase ID token; no raw provider token is transmitted.
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("INVALID_SOCIAL_CREDENTIAL")
        } finally {
            // Tata owns the enduring session; remove the SDK's independent signed-in identity.
            auth.signOut()
            if (provider == SocialProvider.FACEBOOK) LoginManager.getInstance().logOut()
        }
    }

    private suspend fun facebookAccessToken(activity: ComponentActivity): String {
        val appId = context.getString(R.string.tata_facebook_app_id)
        val clientToken = context.getString(R.string.tata_facebook_client_token)
        if (appId.isBlank() || clientToken.isBlank()) throw IllegalStateException("SOCIAL_UNAVAILABLE")
        FacebookSdk.setApplicationId(appId)
        FacebookSdk.setClientToken(clientToken)
        @Suppress("DEPRECATION")
        FacebookSdk.sdkInitialize(context.applicationContext)
        return suspendCancellableCoroutine { continuation ->
            val manager = LoginManager.getInstance()
            manager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    if (continuation.isActive) continuation.resume(result.accessToken.token)
                }
                override fun onCancel() {
                    if (continuation.isActive) continuation.cancel(CancellationException("SOCIAL_CANCELLED"))
                }
                override fun onError(error: FacebookException) {
                    if (continuation.isActive)
                        continuation.resumeWithException(IllegalStateException("SOCIAL_PROVIDER_UNAVAILABLE"))
                }
            })
            continuation.invokeOnCancellation { manager.unregisterCallback(callbackManager) }
            manager.logInWithReadPermissions(activity, listOf("email", "public_profile"))
        }
    }

    /** Routes only Facebook's registered activity result to its callback manager. */
    fun onFacebookActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        callbackManager.onActivityResult(requestCode, resultCode, data)
}
