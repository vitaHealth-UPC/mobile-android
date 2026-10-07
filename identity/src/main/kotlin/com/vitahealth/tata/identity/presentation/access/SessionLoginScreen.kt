package com.vitahealth.tata.identity.presentation.access

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.identity.R
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*

private val LoginInter = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter))

@Composable
fun SessionAccessScreen(state: SessionAccessUiState, onEmail: (String) -> Unit,
    onPassword: (String) -> Unit, onSignIn: () -> Unit, onRegister: () -> Unit,
    onPin: () -> Unit, showPin: Boolean) {
    var information by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val imageLoader = remember(context) { ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build() }
    Column(Modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState())
        .imePadding().padding(horizontal = 22.dp).padding(top = tataPrototypeTopPadding(), bottom = 32.dp)) {
        Text("Inicia sesión", fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
            fontSize = 29.sp, lineHeight = 38.sp, color = tataTextColor(), modifier = Modifier.padding(start = 2.dp))
        LoginText("Accede a tu cuenta para continuar.", 12, tataMutedColor(), modifier = Modifier.padding(top = 2.dp, start = 2.dp))
        Spacer(Modifier.height(46.dp))
        TataFormField("Correo electrónico", state.email, onEmail,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            enabled = !state.busy, softSurface = true)
        Spacer(Modifier.height(22.dp))
        TataFormField("Contraseña", state.password, onPassword, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            enabled = !state.busy, softSurface = true)
        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { information = "Recuperar contraseña" }, enabled = !state.busy,
                contentPadding = PaddingValues(0.dp)) {
                LoginText("¿Olvidaste tu contraseña?", 12, TataNavy, FontWeight.SemiBold)
            }
        }
        state.error?.let { AccessError(it) }
        Surface(onClick = onSignIn, enabled = !state.busy, shape = CircleShape, color = TataNavy,
            shadowElevation = 0.dp, modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape)) {
            Box(contentAlignment = Alignment.Center) {
                LoginText(if (state.busy) "Ingresando…" else "Iniciar sesión", 15, Color.White, FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HorizontalDivider(Modifier.weight(1f), color = TataBorder)
            LoginText("o continúa con", 12, tataMutedColor())
            HorizontalDivider(Modifier.weight(1f), color = TataBorder)
        }
        Spacer(Modifier.height(36.dp))
        listOf("Google" to R.raw.figma_google, "Facebook" to R.raw.figma_facebook).forEachIndexed { index, (provider, asset) ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            Surface(onClick = { information = provider }, enabled = !state.busy,
                shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 0.dp,
                border = BorderStroke(1.dp, TataBorder), modifier = Modifier.fillMaxWidth().height(52.dp).tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    AsyncImage("android.resource://${context.packageName}/$asset", null, imageLoader = imageLoader,
                        modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(14.dp))
                    LoginText("Continuar con $provider", 15, TataNavy, FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            LoginText("¿No tienes cuenta?", 13, tataMutedColor())
            TextButton(onClick = onRegister, enabled = !state.busy) {
                LoginText("Crear cuenta", 13, TataNavy, FontWeight.SemiBold)
            }
        }
        if (showPin) TextButton(onClick = onPin, enabled = !state.busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            LoginText("Ingresar con PIN", 13, TataNavy, FontWeight.SemiBold)
        }
    }
    information?.let { selection ->
        AlertDialog(onDismissRequest = { information = null }, title = { Text(selection) },
            text = { Text(if (selection == "Recuperar contraseña")
                "Solicita ayuda al responsable de tu cuenta para recuperar el acceso."
                else "El acceso con $selection aún no está habilitado. Puedes ingresar con tu correo y contraseña.") },
            confirmButton = { TextButton(onClick = { information = null }) { Text("Entendido") } })
    }
}

@Composable
private fun LoginText(text: String, size: Int, color: Color, weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.bodySmall.copy(fontFamily = LoginInter,
        fontSize = size.sp, lineHeight = (size * 1.3f).sp, letterSpacing = 0.sp, fontWeight = weight), color = color)
}
