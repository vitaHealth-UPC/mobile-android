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
fun SessionAccessScreen(state: SessionAccessUiState, onEmail: (String)->Unit, onPassword: (String)->Unit, onSignIn: ()->Unit, onRegister: ()->Unit, onPin: ()->Unit, showPin: Boolean) {
    Column(Modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState()).padding(horizontal=22.dp).padding(top=32.dp,bottom=24.dp)) {
        Text("Inicia sesiÃ³n",fontFamily=FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),fontSize=29.sp,color=tataTextColor())
        Text("Accede a tu cuenta para continuar.",fontSize=12.sp,color=tataMutedColor())
        Spacer(Modifier.height(48.dp))
        TataFormField("Correo electrÃ³nico",state.email,onEmail,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),enabled=!state.busy)
        Spacer(Modifier.height(22.dp))
        TataFormField("ContraseÃ±a",state.password,onPassword,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),enabled=!state.busy)
        Spacer(Modifier.height(34.dp))
        state.error?.let { AccessError(it) }
        TataButton(if(state.busy) "Ingresandoâ€¦" else "Iniciar sesiÃ³n",onSignIn,enabled=!state.busy)
        Spacer(Modifier.height(24.dp))
        if(showPin) TataButton("Ingresar con PIN",onPin,style=TataButtonStyle.Secondary,enabled=!state.busy)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
            Text("Â¿No tienes cuenta?",fontSize=13.sp,color=tataMutedColor())
            TextButton(onClick=onRegister,enabled=!state.busy) { Text("Crear cuenta",color=TataNavy) }
        }
    }
}

@Composable
fun PinAccessRoute(factory: SessionAccessViewModel.Factory, olderAdultId: String, olderAdultName: String, setup: Boolean, onComplete: ()->Unit, onBack: ()->Unit) {
    val vm: SessionAccessViewModel = viewModel(factory=factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(state.subject,state.pinSaved) { if(state.subject!=null || state.pinSaved) onComplete() }
    PinAccessScreen(state,olderAdultName,setup,vm::digit,{vm.pin(olderAdultId,setup)},onBack)
}

@Composable
fun PinAccessScreen(state: SessionAccessUiState, olderAdultName: String, setup: Boolean, onDigit: (String)->Unit, onSubmit: ()->Unit, onBack: ()->Unit) {
    Column(Modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState()).padding(horizontal=22.dp).padding(top=24.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(if(setup) "Crea tu PIN" else "Ingresa a Tata",fontFamily=FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),fontSize=29.sp,color=tataTextColor())
        Text("Acceso simple para el adulto mayor.",fontSize=12.sp,color=tataMutedColor())
        TataCard(containerColor=TataMint) {
            Text(olderAdultName,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=tataTextColor())
            Text(if(setup) "Elige un PIN de 4 dÃ­gitos." else "Usa tu PIN de 4 dÃ­gitos.",fontSize=12.sp,color=tataMutedColor())
        }
        Text("PIN",fontSize=12.sp,color=tataMutedColor())
        Surface(shape=RoundedCornerShape(18.dp),color=Color.White,shadowElevation=4.dp,modifier=Modifier.fillMaxWidth()) {
            Row(Modifier.padding(22.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                repeat(4) { index -> Box(Modifier.size(32.dp).background(TataLavender,CircleShape),contentAlignment=Alignment.Center) { Text(if(index<state.pin.length) "â€¢" else "",fontSize=22.sp,color=TataNavy) } }
            }
        }
        Spacer(Modifier.height(8.dp))
        listOf(listOf("1","2","3"),listOf("4","5","6"),listOf("7","8","9"),listOf("","0","âŒ«")).forEach { row ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround) {
                row.forEach { digit -> if(digit.isBlank()) Spacer(Modifier.size(54.dp)) else Surface(onClick={onDigit(digit)},enabled=!state.busy,shape=CircleShape,color=Color.White,shadowElevation=4.dp,modifier=Modifier.size(54.dp)) { Box(contentAlignment=Alignment.Center) { Text(digit,fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=TataDeepNavy) } } }
            }
        }
        state.error?.let { AccessError(it) }
        Spacer(Modifier.height(12.dp))
        TataButton(if(state.busy) "Procesandoâ€¦" else if(setup) "Guardar PIN" else "Ingresar",onSubmit,enabled=!state.busy && state.pin.length==4)
        Text("Â¿Olvidaste tu PIN? Pide ayuda a tu familiar.",fontSize=12.sp,color=tataMutedColor())
        TextButton(onClick=onBack) { Text("Volver") }
    }
}

@Composable
private fun AccessError(code: String) {
    val message=when(code) {
        "PIN_LOCKED","PIN_TEMPORARILY_BLOCKED" -> "Tu PIN estÃ¡ bloqueado temporalmente. Intenta nuevamente en 15 minutos."
        "INVALID_PIN","INVALID_CREDENTIALS","PIN_INCORRECT" -> "Los datos de acceso no son correctos. IntÃ©ntalo otra vez."
        "ACCOUNT_NOT_ACTIVE","CONSENT_REQUIRED" -> "Verifica tu cuenta y completa la vinculaciÃ³n para continuar."
        "NETWORK_UNAVAILABLE" -> "No hay conexiÃ³n. Revisa tu red e intÃ©ntalo otra vez."
        "REQUIRED_FIELDS" -> "Completa tu correo y contraseÃ±a."
        else -> "No pudimos completar el acceso. IntÃ©ntalo nuevamente."
    }
    Text(message,color=TataError,modifier=Modifier.fillMaxWidth().padding(vertical=8.dp))
}
