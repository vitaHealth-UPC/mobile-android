package com.vitahealth.tata.identity.presentation.access

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.application.SessionSubject
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*

@Composable
fun SessionAccessRoute(factory: SessionAccessViewModel.Factory, onAuthenticated: (SessionSubject) -> Unit, onRegister: () -> Unit, onPin: () -> Unit, showPin: Boolean) {
    val vm: SessionAccessViewModel = viewModel(factory=factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.restore() }
    LaunchedEffect(state.subject) { state.subject?.let(onAuthenticated) }
    SessionAccessScreen(state,vm::email,vm::password,vm::signIn,onRegister,onPin,showPin)
}

@Composable
fun PinAccessRoute(factory: SessionAccessViewModel.Factory, olderAdultId: String, olderAdultName: String, setup: Boolean, onComplete: ()->Unit, onBack: ()->Unit) {
    val vm: SessionAccessViewModel = viewModel(factory=factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(state.subject,state.pinSaved) { if(state.subject!=null || state.pinSaved) onComplete() }
    PinAccessScreen(state,olderAdultName,setup,vm::digit,{vm.pin(olderAdultId,setup)},onBack)
}

@Composable
internal fun AccessError(code: String) {
    val message=when(code) {
        "PIN_LOCKED","PIN_TEMPORARILY_BLOCKED" -> "Tu PIN está bloqueado temporalmente. Intenta nuevamente en 15 minutos."
        "INVALID_PIN","INVALID_CREDENTIALS","PIN_INCORRECT" -> "Los datos de acceso no son correctos. Inténtalo otra vez."
        "ACCOUNT_NOT_ACTIVE","CONSENT_REQUIRED" -> "Verifica tu cuenta y completa la vinculación para continuar."
        "NETWORK_UNAVAILABLE" -> "No hay conexión. Revisa tu red e inténtalo otra vez."
        "REQUIRED_FIELDS" -> "Completa tu correo y contraseña."
        else -> "No pudimos completar el acceso. Inténtalo nuevamente."
    }
    Text(message,color=TataError,modifier=Modifier.fillMaxWidth().padding(vertical=8.dp))
}
