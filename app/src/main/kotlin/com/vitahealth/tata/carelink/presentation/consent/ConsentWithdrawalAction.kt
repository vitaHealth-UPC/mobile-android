package com.vitahealth.tata.carelink.presentation.consent

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R

@Composable
fun ConsentWithdrawalAction(factory: ConsentWithdrawalViewModel.Factory, onWithdrawn: () -> Unit) {
    val vm: ConsentWithdrawalViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(state.withdrawn) { if (state.withdrawn) onWithdrawn() }
    ConsentWithdrawalControl(state, vm::request, vm::cancel, vm::confirm)
}

@Composable
fun ConsentWithdrawalControl(
    state: ConsentWithdrawalUiState,
    onRequest: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    // Resolve localized labels in the screen context before creating the dialog window.
    val action = stringResource(R.string.consent_withdraw_action)
    val title = stringResource(R.string.consent_withdraw_title)
    val explanation = stringResource(R.string.consent_withdraw_explanation)
    val cancel = stringResource(R.string.consent_withdraw_cancel)
    val confirm =
        stringResource(
            if (state.busy) R.string.consent_withdraw_busy else R.string.consent_withdraw_confirm
        )
    val error =
        stringResource(
            if (
                state.errorCode in
                    setOf("SESSION_CHANGED", "AUTHENTICATION_REQUIRED", "RESOURCE_ACCESS_DENIED")
            )
                R.string.consent_withdraw_access_error
            else R.string.consent_withdraw_error
        )
    TextButton(onClick = onRequest, enabled = !state.busy && !state.withdrawn) { Text(action) }
    if (state.confirming)
        AlertDialog(
            onDismissRequest = { if (!state.busy) onCancel() },
            title = { Text(title) },
            text = {
                Column {
                    Text(explanation)
                    if (state.errorCode != null)
                        Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm, enabled = !state.busy) { Text(confirm) }
            },
            dismissButton = {
                TextButton(onClick = onCancel, enabled = !state.busy) { Text(cancel) }
            },
        )
}
