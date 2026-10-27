package com.vitahealth.tata.identity.presentation.access.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.identity.presentation.access.ui.DemoCredentials
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.components.TataPasswordField
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

private val LoginInter = FontFamily(Font(R.font.tata_inter))

@Composable
fun SessionAccessScreen(
    state: SessionAccessUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSignIn: () -> Unit,
    onUseDemoAccount: () -> Unit,
    onRegister: () -> Unit,
    onPin: () -> Unit,
    showPin: Boolean,
    onRecover: (() -> Unit)? = null,
    onGoogle: (() -> Unit)? = null,
    onFacebook: (() -> Unit)? = null,
    socialBusy: Boolean = false,
) {
    var information by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val imageLoader =
        remember(context) {
            ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
        }
    Column(
        Modifier.fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 22.dp)
            .padding(top = 28.dp, bottom = 32.dp)
    ) {
        Text(
            "Inicia sesión",
            fontFamily = FontFamily(Font(R.font.tata_serif)),
            fontSize = 29.sp,
            lineHeight = 38.sp,
            color = tataTextColor(),
            modifier = Modifier.padding(start = 2.dp),
        )
        LoginText(
            "Accede a tu cuenta para continuar.",
            12,
            tataMutedColor(),
            modifier = Modifier.padding(top = 2.dp, start = 2.dp),
        )
        Spacer(Modifier.height(46.dp))
        TataFormField(
            "Correo electrónico",
            state.email,
            onEmail,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            enabled = !state.busy,
            softSurface = true,
        )
        DemoAccountHint(enabled = !state.busy, onUseDemoAccount = onUseDemoAccount)
        Spacer(Modifier.height(18.dp))
        TataPasswordField(
            label = "Contraseña",
            value = state.password,
            onValueChange = onPassword,
            enabled = !state.busy,
            softSurface = true,
            imeAction = ImeAction.Done,
        )
        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterEnd) {
            TextButton(
                onClick = { onRecover?.invoke() ?: run { information = "Recuperar contraseña" } },
                enabled = !state.busy,
                contentPadding = PaddingValues(0.dp),
            ) {
                LoginText("¿Olvidaste tu contraseña?", 12, TataNavy, FontWeight.SemiBold)
            }
        }
        state.error?.let { AccessError(it) }
        Surface(
            onClick = onSignIn,
            enabled = !state.busy,
            shape = CircleShape,
            color = TataNavy,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape),
        ) {
            Box(contentAlignment = Alignment.Center) {
                LoginText(
                    if (state.busy) "Ingresando…" else "Iniciar sesión",
                    15,
                    Color.White,
                    FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(30.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalDivider(Modifier.weight(1f), color = TataBorder)
            LoginText("o continúa con", 12, tataMutedColor())
            HorizontalDivider(Modifier.weight(1f), color = TataBorder)
        }
        Spacer(Modifier.height(36.dp))
        listOf("Google" to R.raw.figma_google, "Facebook" to R.raw.figma_facebook).forEachIndexed {
            index,
            (provider, asset) ->
            if (index > 0) Spacer(Modifier.height(12.dp))
            Surface(
                onClick = {
                    val callback = if (provider == "Google") onGoogle else onFacebook
                    callback?.invoke() ?: run { information = provider }
                },
                enabled = !state.busy && !socialBusy,
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, TataBorder),
                modifier =
                    Modifier.fillMaxWidth()
                        .height(52.dp)
                        .tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AsyncImage(
                        "android.resource://${context.packageName}/$asset",
                        null,
                        imageLoader = imageLoader,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    LoginText("Continuar con $provider", 15, TataNavy, FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoginText("¿No tienes cuenta?", 13, tataMutedColor())
            TextButton(onClick = onRegister, enabled = !state.busy) {
                LoginText("Crear cuenta", 13, TataNavy, FontWeight.SemiBold)
            }
        }
        if (showPin)
            TextButton(
                onClick = onPin,
                enabled = !state.busy,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                LoginText("Ingresar con PIN", 13, TataNavy, FontWeight.SemiBold)
            }
    }
    information?.let { selection ->
        AlertDialog(
            onDismissRequest = { information = null },
            title = { Text(selection) },
            text = {
                Text(
                    if (selection == "Recuperar contraseña")
                        "Solicita ayuda al responsable de tu cuenta para recuperar el acceso."
                    else
                        "El acceso con $selection aún no está habilitado. Puedes ingresar con tu correo y contraseña."
                )
            },
            confirmButton = { TextButton(onClick = { information = null }) { Text("Entendido") } },
        )
    }
}

@Composable
private fun DemoAccountHint(enabled: Boolean, onUseDemoAccount: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            LoginText("Usa esta cuenta demo", 11, tataMutedColor())
            LoginText(
                DemoCredentials.EMAIL,
                13,
                TataNavy,
                FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        TextButton(
            onClick = onUseDemoAccount,
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            LoginText("Usar", 12, TataNavy, FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LoginText(
    text: String,
    size: Int,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = LoginInter,
                fontSize = size.sp,
                lineHeight = (size * 1.3f).sp,
                letterSpacing = 0.sp,
                fontWeight = weight,
            ),
        color = color,
    )
}
