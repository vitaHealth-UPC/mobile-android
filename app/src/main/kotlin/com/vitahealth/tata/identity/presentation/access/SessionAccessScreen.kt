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
    SessionAccessScreen(state,vm::email,vm::password,vm::signIn,vm::useDemoAccount,onRegister,onPin,showPin)
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
    Text(sessionAccessMessage(code), color = TataError, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
}
