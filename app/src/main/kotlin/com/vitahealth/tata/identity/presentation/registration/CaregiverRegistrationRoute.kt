package com.vitahealth.tata.identity.presentation.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.presentation.registration.screens.CaregiverRegistrationScreen
import com.vitahealth.tata.identity.presentation.registration.viewmodel.CaregiverRegistrationViewModel
import com.vitahealth.tata.identity.presentation.registration.viewmodel.RegistrationStep
@Composable
fun CaregiverRegistrationRoute(
    factory: CaregiverRegistrationViewModel.Factory,
    onRegistrationComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CaregiverRegistrationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.step, state.accountId) {
        val accountId = state.accountId
        if (state.step == RegistrationStep.Complete && accountId != null) {
            onRegistrationComplete(accountId)
        }
    }

    CaregiverRegistrationScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onVerificationCodeChange = viewModel::onVerificationCodeChange,
        onCreateAccount = viewModel::createAccount,
        onVerifyEmail = viewModel::verifyEmail,
        onRequestNewVerification = viewModel::requestNewVerification,
        modifier = modifier,
    )
}
