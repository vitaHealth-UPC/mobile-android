package com.vitahealth.tata.identity.presentation.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R

@Composable
fun PasswordRecoveryDialog(vm: PasswordRecoveryViewModel, onClose: () -> Unit) {
    val state by vm.state.collectAsState()
    val title = stringResource(R.string.recovery_title)
    val emailLabel = stringResource(R.string.recovery_email)
    val codeLabel = stringResource(R.string.recovery_code)
    val passwordLabel = stringResource(R.string.recovery_password)
    val notice =
        stringResource(
            when (state.step) {
                RecoveryStep.Email -> R.string.recovery_intro
                RecoveryStep.Code -> R.string.recovery_sent
                RecoveryStep.Complete -> R.string.recovery_complete
            }
        )
    val action =
        stringResource(
            when {
                state.busy -> R.string.recovery_busy
                state.step == RecoveryStep.Email -> R.string.recovery_send
                state.step == RecoveryStep.Code -> R.string.recovery_save
                else -> R.string.recovery_return
            }
        )
    val cancel = stringResource(R.string.recovery_cancel)
    val error =
        stringResource(
            when (state.error) {
                "INVALID_RECOVERY_CODE" -> R.string.recovery_invalid_code
                "INVALID_EMAIL" -> R.string.recovery_invalid_email
                "VALIDATION_ERROR" -> R.string.recovery_invalid_fields
                "NETWORK_UNAVAILABLE" -> R.string.recovery_network
                else -> R.string.recovery_error
            }
        )
    fun close() {
        if (!state.busy) {
            vm.reset()
            onClose()
        }
    }
    AlertDialog(
        onDismissRequest = ::close,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(notice)
                if (state.step == RecoveryStep.Email)
                    OutlinedTextField(
                        state.email,
                        vm::email,
                        Modifier.testTag("recovery-email"),
                        label = { Text(emailLabel) },
                        enabled = !state.busy,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                if (state.step == RecoveryStep.Code) {
                    OutlinedTextField(
                        state.code,
                        vm::code,
                        Modifier.testTag("recovery-code"),
                        label = { Text(codeLabel) },
                        enabled = !state.busy,
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )
                    OutlinedTextField(
                        state.password,
                        vm::password,
                        Modifier.testTag("recovery-password"),
                        label = { Text(passwordLabel) },
                        enabled = !state.busy,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                }
                if (state.error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.busy,
                onClick = {
                    when (state.step) {
                        RecoveryStep.Email -> vm.request()
                        RecoveryStep.Code -> vm.complete()
                        RecoveryStep.Complete -> close()
                    }
                },
            ) {
                Text(action)
            }
        },
        dismissButton = {
            if (state.step != RecoveryStep.Complete)
                TextButton(onClick = ::close, enabled = !state.busy) { Text(cancel) }
        },
    )
}
