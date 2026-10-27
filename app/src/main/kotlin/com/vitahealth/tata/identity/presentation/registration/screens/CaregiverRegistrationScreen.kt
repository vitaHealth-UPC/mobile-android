package com.vitahealth.tata.identity.presentation.registration.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.presentation.registration.ui.RegistrationButton
import com.vitahealth.tata.identity.presentation.registration.ui.RegistrationMessage
import com.vitahealth.tata.identity.presentation.registration.ui.RegistrationProgress
import com.vitahealth.tata.identity.presentation.registration.ui.RegistrationText
import com.vitahealth.tata.identity.presentation.registration.ui.VerificationCard
import com.vitahealth.tata.identity.presentation.registration.viewmodel.CaregiverRegistrationUiState
import com.vitahealth.tata.identity.presentation.registration.viewmodel.CaregiverRegistrationViewModel
import com.vitahealth.tata.identity.presentation.registration.viewmodel.RegistrationStep
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.components.TataPasswordField
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarning
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarningSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeTopPadding
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
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
    val accountEditable = state.step == RegistrationStep.Account && !state.isLoading
    Column(
        modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = tataPrototypeTopPadding(), bottom = 24.dp)
    ) {
        Text(
            "Crea tu cuenta",
            fontFamily = FontFamily(Font(R.font.tata_serif)),
            fontSize = 29.sp,
            lineHeight = 38.sp,
            color = tataTextColor(),
            modifier = Modifier.padding(start = 2.dp),
        )
        RegistrationText(
            "Para acompañar y dar seguimiento.",
            12,
            tataMutedColor(),
            modifier = Modifier.padding(start = 2.dp, top = 2.dp),
        )
        RegistrationProgress(state.step != RegistrationStep.Account, Modifier.padding(top = 14.dp))
        Spacer(Modifier.height(16.dp))
        TataFormField(
            label = "Nombre",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Diego Mendoza",
            enabled = accountEditable,
            softSurface = true,
        )
        Spacer(Modifier.height(2.dp))
        TataFormField(
            label = "Correo",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "diego@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = accountEditable,
            softSurface = true,
        )
        Spacer(Modifier.height(2.dp))
        TataPasswordField(
            label = "Contraseña",
            value = state.password,
            onValueChange = onPasswordChange,
            enabled = accountEditable,
            softSurface = true,
        )
        Spacer(Modifier.height(10.dp))
        RegistrationButton(
            if (state.isLoading && state.step == RegistrationStep.Account) "Procesando…"
            else "Crear cuenta",
            onCreateAccount,
            accountEditable,
        )
        Spacer(Modifier.height(20.dp))
        VerificationCard(
            state.verificationCode,
            onVerificationCodeChange,
            enabled = state.step == RegistrationStep.Verification && !state.isLoading,
            codeSent = state.step != RegistrationStep.Account,
        )
        Spacer(Modifier.height(22.dp))
        RegistrationButton(
            when (state.step) {
                RegistrationStep.VerificationExpired ->
                    if (state.isLoading) "Solicitando…" else "Solicitar nuevo código"
                RegistrationStep.Complete -> "Correo verificado"
                else ->
                    if (state.isLoading && state.step == RegistrationStep.Verification)
                        "Verificando…"
                    else "Verificar correo"
            },
            if (state.step == RegistrationStep.VerificationExpired) onRequestNewVerification
            else onVerifyEmail,
            enabled =
                !state.isLoading &&
                    (state.step == RegistrationStep.VerificationExpired ||
                        (state.step == RegistrationStep.Verification &&
                            state.verificationCode.length == 6)),
            secondary = true,
        )
        if (state.step == RegistrationStep.VerificationExpired) {
            Spacer(Modifier.height(16.dp))
            RegistrationMessage(
                "Verificación vencida",
                "Solicita un nuevo código para habilitar la cuenta.",
                TataWarningSurface,
                TataWarning,
            )
        }
        state.errorMessage?.let { error ->
            Spacer(Modifier.height(16.dp))
            if (state.errorCode == "DUPLICATE_EMAIL") {
                RegistrationMessage(
                    "Correo ya registrado",
                    "Utiliza otro correo o inicia sesión con la cuenta existente.",
                    TataErrorSurface,
                    TataError,
                )
            } else
                RegistrationMessage(
                    "No pudimos completar la solicitud",
                    error,
                    TataErrorSurface,
                    TataError,
                )
        }
    }
}
