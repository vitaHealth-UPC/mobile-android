package com.vitahealth.tata.identity.presentation.access

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.presentation.access.mappers.sessionAccessMessageRes
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Links a provider only after the user is authenticated with an existing Tata caregiver account. */
@Composable
fun SocialIdentityLinkDialog(
    credentials: SocialCredentialLauncher,
    link: suspend (String) -> AppResult<Unit>,
    onDismiss: () -> Unit,
) {
    val activity = LocalContext.current as? ComponentActivity
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }

    val choose: (SocialProvider) -> Unit = { provider ->
        if (!busy && !completed) {
            if (activity == null || !credentials.isAvailable(provider)) {
                error = "SOCIAL_UNAVAILABLE"
            } else {
                busy = true
                error = null
                scope.launch {
                    try {
                        val verifiedEvidence = credentials.request(provider, activity)
                        when (val outcome = link(verifiedEvidence)) {
                            is AppResult.Success -> completed = true
                            is AppResult.Failure -> error = outcome.code ?: "REQUEST_FAILED"
                        }
                    } catch (cancel: CancellationException) {
                        if (cancel.message != "SOCIAL_CANCELLED") throw cancel
                    } catch (_: Exception) {
                        error = "SOCIAL_PROVIDER_UNAVAILABLE"
                    } finally {
                        busy = false
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(R.string.access_social_link_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (completed) stringResource(R.string.access_social_link_success)
                    else stringResource(R.string.access_social_link_description)
                )
                if (busy) CircularProgressIndicator()
                error?.let { Text(stringResource(sessionAccessMessageRes(it))) }
            }
        },
        dismissButton = {
            if (!completed) {
                Row {
                    TextButton(onClick = { choose(SocialProvider.GOOGLE) }, enabled = !busy) {
                        Text("Google")
                    }
                    TextButton(onClick = { choose(SocialProvider.FACEBOOK) }, enabled = !busy) {
                        Text("Facebook")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(stringResource(R.string.access_social_link_close))
            }
        },
    )
}
