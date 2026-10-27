package com.vitahealth.tata.identity.presentation.access

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.application.ports.PasswordRecovery
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.identity.presentation.access.screens.SessionAccessScreen
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessViewModel
import com.vitahealth.tata.identity.presentation.recovery.PasswordRecoveryDialog
import com.vitahealth.tata.identity.presentation.recovery.PasswordRecoveryViewModel

@Composable
fun SessionAccessRoute(
    factory: SessionAccessViewModel.Factory,
    onAuthenticated: (SessionSubject) -> Unit,
    onRegister: () -> Unit,
    onPin: () -> Unit,
    showPin: Boolean,
    recovery: PasswordRecovery? = null,
    socialCredentials: SocialCredentialLauncher? = null,
) {
    val vm: SessionAccessViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.restore() }
    LaunchedEffect(state.subject) { state.subject?.let(onAuthenticated) }
    var recovering by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? ComponentActivity
    val scope = rememberCoroutineScope()
    var requestingSocial by remember { mutableStateOf(false) }
    val attemptSocial: (SocialProvider) -> Unit = { provider ->
        if (!requestingSocial && !state.busy) {
            if (activity == null || socialCredentials?.isAvailable(provider) != true) {
                vm.socialError("SOCIAL_UNAVAILABLE")
            } else {
                requestingSocial = true
                scope.launch {
                    try {
                        val verifiedEvidence = socialCredentials.request(provider, activity)
                        vm.socialToken(verifiedEvidence)
                    } catch (cancelled: CancellationException) {
                        // User cancellation must not start a Tata session.
                        if (cancelled.message != "SOCIAL_CANCELLED") throw cancelled
                    } catch (failure: Exception) {
                        vm.socialError(
                            if (failure.message in setOf("SOCIAL_UNAVAILABLE", "INVALID_SOCIAL_CREDENTIAL"))
                                failure.message!! else "SOCIAL_PROVIDER_UNAVAILABLE"
                        )
                    } finally {
                        requestingSocial = false
                    }
                }
            }
        }
    }

    SessionAccessScreen(
        state,
        vm::email,
        vm::password,
        vm::signIn,
        vm::useDemoAccount,
        onRegister,
        onPin,
        showPin,
        onRecover = recovery?.let { { recovering = true } },
        onGoogle = { attemptSocial(SocialProvider.GOOGLE) },
        onFacebook = { attemptSocial(SocialProvider.FACEBOOK) },
        socialBusy = requestingSocial,

    )
    if (recovering && recovery != null) {
        val recoveryVm: PasswordRecoveryViewModel =
            viewModel(
                key = "password-recovery",
                factory =
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>) =
                            PasswordRecoveryViewModel(recovery) as T
                    },
            )
        PasswordRecoveryDialog(recoveryVm) { recovering = false }
    }
}
