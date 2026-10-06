package com.vitahealth.tata.identity.presentation.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface

@Composable
fun CaregiverRegistrationRoute(
    factory: CaregiverRegistrationViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: CaregiverRegistrationViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
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

@Composable
fun CaregiverRegistrationScreen(
    state: CaregiverRegistrationUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onCreateAccount: () -> Unit,
    onVerifyEmail: () -> Unit,
    onRequestNewVerification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium,
            color = TataText,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Para acompañar y dar seguimiento.",
            style = MaterialTheme.typography.bodySmall,
            color = TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )

        RegistrationProgress(
            verificationActive = state.step != RegistrationStep.Account,
            modifier = Modifier.padding(top = 24.dp),
        )

        Spacer(Modifier.height(16.dp))
        TataFormField(
            label = "Nombre",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Diego Mendoza",
        )
        Spacer(Modifier.height(12.dp))
        TataFormField(
            label = "Correo",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "diego@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        TataFormField(
            label = "Contraseña",
            value = state.password,
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(Modifier.height(14.dp))
        TataButton(
            text = if (state.isLoading && state.step == RegistrationStep.Account) {
                "Procesando..."
            } else {
                "Crear cuenta"
            },
            enabled = !state.isLoading && state.step == RegistrationStep.Account,
            onClick = onCreateAccount,
        )

        if (state.step != RegistrationStep.Account) {
            Spacer(Modifier.height(20.dp))
            VerificationCard(
                code = state.verificationCode,
                onCodeChange = onVerificationCodeChange,
            )
            Spacer(Modifier.height(14.dp))

            when (state.step) {
                RegistrationStep.Verification -> TataButton(
                    text = if (state.isLoading) "Verificando..." else "Verificar correo",
                    enabled = !state.isLoading,
                    onClick = onVerifyEmail,
                    style = TataButtonStyle.Secondary,
                )

                RegistrationStep.VerificationExpired -> TataButton(
                    text = if (state.isLoading) "Solicitando..." else "Solicitar nuevo código",
                    enabled = !state.isLoading,
                    onClick = onRequestNewVerification,
                    style = TataButtonStyle.Secondary,
                )

                RegistrationStep.Complete -> TataButton(
                    text = "Correo verificado",
                    enabled = false,
                    onClick = {},
                    style = TataButtonStyle.Secondary,
                )

                RegistrationStep.Account -> Unit
            }
        }

        if (state.step == RegistrationStep.VerificationExpired) {
            Spacer(Modifier.height(16.dp))
            VerificationExpiredMessage()
        }

        state.errorMessage?.let { error ->
            Spacer(Modifier.height(14.dp))
            TataCard(containerColor = TataErrorSurface) {
                Text(error, color = TataError, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun RegistrationProgress(
    verificationActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TataLavender, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("1  Cuenta", color = TataDeepNavy, fontWeight = FontWeight.SemiBold)
        Text(
            "2  Verificación",
            color = if (verificationActive) TataDeepNavy else TataMuted,
            fontWeight = if (verificationActive) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun VerificationCard(
    code: String,
    onCodeChange: (String) -> Unit,
) {
    TataCard(containerColor = TataCream) {
        Text("Verifica tu correo", color = TataText, fontWeight = FontWeight.SemiBold)
        Text(
            "Te enviamos un código de 6 dígitos. Escríbelo para habilitar tu cuenta.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
        )
        TataFormField(
            label = "Código",
            value = code,
            onValueChange = onCodeChange,
            placeholder = "4  8  2  1  9  6",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
    }
}

@Composable
private fun VerificationExpiredMessage() {
    TataCard(containerColor = TataWarningSurface) {
        Text(
            text = "Verificación vencida",
            color = TataWarning,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Solicita un nuevo código para habilitar la cuenta.",
            color = TataMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
