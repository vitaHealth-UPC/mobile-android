package com.vitahealth.tata.identity.presentation.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.tataPrototypeTopPadding
import com.vitahealth.tata.shared.design.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.design.theme.tataTextColor
import com.vitahealth.tata.shared.design.theme.tataMutedColor
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.components.TataPasswordField
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface

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
    Column(modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp).padding(top = tataPrototypeTopPadding(), bottom = 24.dp)) {
        Text("Crea tu cuenta", fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif)),
            fontSize = 29.sp, lineHeight = 38.sp, color = tataTextColor(), modifier = Modifier.padding(start = 2.dp))
        RegistrationText("Para acompañar y dar seguimiento.", 12, tataMutedColor(), modifier = Modifier.padding(start = 2.dp, top = 2.dp))
        RegistrationProgress(state.step != RegistrationStep.Account, Modifier.padding(top = 14.dp))
        Spacer(Modifier.height(16.dp))
        TataFormField(label = "Nombre", value = state.name, onValueChange = onNameChange,
            placeholder = "Diego Mendoza", enabled = accountEditable, softSurface = true)
        Spacer(Modifier.height(2.dp))
        TataFormField(label = "Correo", value = state.email, onValueChange = onEmailChange,
            placeholder = "diego@email.com", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = accountEditable, softSurface = true)
        Spacer(Modifier.height(2.dp))
        TataPasswordField(
            label = "Contraseña",
            value = state.password,
            onValueChange = onPasswordChange,
            enabled = accountEditable,
            softSurface = true,
        )
        Spacer(Modifier.height(10.dp))
        RegistrationButton(if (state.isLoading && state.step == RegistrationStep.Account) "Procesando…" else "Crear cuenta",
            onCreateAccount, accountEditable)
        Spacer(Modifier.height(20.dp))
        VerificationCard(state.verificationCode, onVerificationCodeChange,
            enabled = state.step == RegistrationStep.Verification && !state.isLoading,
            codeSent = state.step != RegistrationStep.Account)
        Spacer(Modifier.height(22.dp))
        RegistrationButton(when (state.step) {
            RegistrationStep.VerificationExpired -> if (state.isLoading) "Solicitando…" else "Solicitar nuevo código"
            RegistrationStep.Complete -> "Correo verificado"
            else -> if (state.isLoading && state.step == RegistrationStep.Verification) "Verificando…" else "Verificar correo"
        }, if (state.step == RegistrationStep.VerificationExpired) onRequestNewVerification else onVerifyEmail,
            enabled = !state.isLoading && (state.step == RegistrationStep.VerificationExpired ||
                (state.step == RegistrationStep.Verification && state.verificationCode.length == 6)), secondary = true)
        if (state.step == RegistrationStep.VerificationExpired) {
            Spacer(Modifier.height(16.dp))
            RegistrationMessage("Verificación vencida", "Solicita un nuevo código para habilitar la cuenta.", TataWarningSurface, TataWarning)
        }
        state.errorMessage?.let { error ->
            Spacer(Modifier.height(16.dp))
            if (state.errorCode == "DUPLICATE_EMAIL") {
                RegistrationMessage("Correo ya registrado", "Utiliza otro correo o inicia sesión con la cuenta existente.", TataErrorSurface, TataError)
            } else RegistrationMessage("No pudimos completar la solicitud", error, TataErrorSurface, TataError)
        }
    }
}

@Composable
private fun RegistrationProgress(verificationActive: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(32.dp).background(Brush.horizontalGradient(listOf(TataLavender, Color(0xFFECF4FB))), RoundedCornerShape(16.dp))
        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RegistrationText("1  Cuenta", 11, TataDeepNavy, FontWeight.SemiBold)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.width(72.dp).height(4.dp).background(TataDeepNavy, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(22.dp))
        RegistrationText("2  Verificación", 11, if (verificationActive) TataDeepNavy else tataMutedColor(),
            if (verificationActive) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun VerificationCard(code: String, onCodeChange: (String) -> Unit, enabled: Boolean, codeSent: Boolean) {
    Surface(shape = RoundedCornerShape(18.dp), color = TataCream,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
        Column(Modifier.heightIn(min = 126.dp).padding(horizontal = 16.dp, vertical = 14.dp)) {
            RegistrationText("Verifica tu correo", 16, tataTextColor(), FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            RegistrationText(if (codeSent) "Te enviamos un código de 6 dígitos. Escríbelo para habilitar tu cuenta."
                else "Crea tu cuenta para recibir un código de 6 dígitos y habilitar tu acceso.", 12, tataMutedColor())
            Spacer(Modifier.height(8.dp))
            BasicTextField(value = code, onValueChange = { value -> onCodeChange(value.filter(Char::isDigit).take(6)) },
                enabled = enabled, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter)),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TataNavy, letterSpacing = 6.sp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp).semantics { contentDescription = "Código de verificación" })
        }
    }
}

@Composable
private fun RegistrationButton(text: String, onClick: () -> Unit, enabled: Boolean, secondary: Boolean = false) {
    Surface(onClick = onClick, enabled = enabled, shape = CircleShape,
        color = if (secondary) TataLavender else TataNavy, contentColor = if (secondary) TataNavy else Color.White,
        border = if (secondary) BorderStroke(1.dp, Color(0xFFC2B5E5)) else null,
        modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape)) {
        Box(contentAlignment = Alignment.Center) {
            RegistrationText(text, 15, if (secondary) TataNavy else Color.White, FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RegistrationMessage(title: String, body: String, background: Color, tint: Color) {
    Column(Modifier.fillMaxWidth().background(background, RoundedCornerShape(16.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp)) {
        RegistrationText(title, 11, tint, FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(body, style = TextStyle(fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter)),
            fontSize = 8.5.sp, lineHeight = 12.sp, color = tataMutedColor()))
    }
}

@Composable
private fun RegistrationText(text: String, size: Int, color: Color, weight: FontWeight = FontWeight.Normal, modifier: Modifier = Modifier) {
    Text(text, modifier, style = TextStyle(fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter)),
        fontSize = size.sp, lineHeight = (size * 1.3f).sp, fontWeight = weight), color = color)
}
