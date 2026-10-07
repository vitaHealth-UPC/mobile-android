package com.vitahealth.tata.identity.presentation.access

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.vitahealth.tata.identity.R
import com.vitahealth.tata.shared.design.theme.*

private val PinInter = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter))
private val PinSerif = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif))

@Composable
fun PinAccessScreen(state: SessionAccessUiState, olderAdultName: String, setup: Boolean,
    onDigit: (String) -> Unit, onSubmit: () -> Unit, onBack: () -> Unit) {
    val locked = state.error in setOf("PIN_LOCKED", "PIN_TEMPORARILY_BLOCKED")
    val incorrect = state.error in setOf("INVALID_PIN", "INVALID_CREDENTIALS", "PIN_INCORRECT")
    Box(Modifier.fillMaxSize().background(TataSurface)) {
        // Original SVG effect bounds, rasterized at 4x with transparency.
        Image(painterResource(R.drawable.figma_pin_glow), null,
            Modifier.offset(x = 92.dp, y = tataPrototypeTopPadding() - 32.dp).size(346.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
            .padding(top = tataPrototypeTopPadding(), bottom = 24.dp)) {
            Text(if (setup) "Crea tu PIN" else "Ingresa a Tata", fontFamily = PinSerif,
                fontSize = 29.sp, lineHeight = 38.sp, color = tataTextColor(), modifier = Modifier.padding(start = 2.dp))
            PinText(if (setup) "Elige 4 dígitos para futuros ingresos." else "Acceso simple para el adulto mayor.",
                12, tataMutedColor(), modifier = Modifier.padding(start = 2.dp, top = 2.dp))
            Spacer(Modifier.height(22.dp))
            Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 0.dp, color = Color.Transparent, modifier = Modifier.tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
                Box(Modifier.fillMaxWidth().heightIn(min = 90.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE8F5EB), Color(0xFFDDEDE1))))) {
                    Row(Modifier.padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.figma_pin_avatar), null,
                            Modifier.size(62.dp).clip(CircleShape))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f).padding(top = 4.dp)) {
                            PinText(olderAdultName, 18, tataTextColor(), FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            PinText(if (setup) "Crea un PIN de 4 dígitos." else "Usa tu PIN de 4 dígitos.", 12, tataMutedColor())
                        }
                    }
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 15.dp)
                        .background(Color.White, CircleShape).padding(horizontal = 16.dp, vertical = 5.dp)) {
                        PinText("Adulto mayor", 10, TataDeepNavy, FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PinText("PIN", 12, tataMutedColor(), FontWeight.Medium, Modifier.padding(start = 2.dp))
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 0.dp, color = Color.White,
                border = BorderStroke(1.dp, TataBorder), modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
                Row(Modifier.height(78.dp).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    repeat(4) { index -> Box(Modifier.size(32.dp).background(TataLavender, CircleShape)
                        .border(1.dp, TataBorder, CircleShape), contentAlignment = Alignment.Center) {
                        PinText(if (index < state.pin.length) "•" else "", 22, TataNavy, FontWeight.Bold)
                    } }
                }
            }
            Spacer(Modifier.height(34.dp))
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "\u232b"))
                .forEachIndexed { rowIndex, digits ->
                    Row(Modifier.fillMaxWidth().height(54.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        digits.forEach { digit ->
                            Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                                if (digit.isNotEmpty()) {
                                    Box(Modifier.fillMaxSize().tataPrototypeShadow(8.dp, CircleShape)
                                        .background(Color.White, CircleShape).clip(CircleShape)
                                        .clickable(enabled = !state.busy && !locked, onClick = { onDigit(digit) }),
                                        contentAlignment = Alignment.Center) {
                                        PinText(digit, if (digit == "\u232b") 18 else 19, TataDeepNavy, FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                    if (rowIndex < 3) Spacer(Modifier.height(6.dp))
                }
            Spacer(Modifier.height(if (setup || state.error != null) 8.dp else 34.dp))
            Surface(onClick = onSubmit, enabled = !state.busy && !locked && state.pin.length == 4,
                shape = CircleShape, color = TataNavy, contentColor = Color.White, shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape)) {
                Box(contentAlignment = Alignment.Center) {
                    PinText(when { state.busy -> "Procesando…"; locked -> "Intenta más tarde"; setup -> "Guardar PIN"; else -> "Ingresar" },
                        15, Color.White, FontWeight.SemiBold)
                }
            }
            if (setup || state.error != null) {
                Spacer(Modifier.height(10.dp))
                Column(Modifier.fillMaxWidth().background(if (state.error != null) Color(0xFFFCE8EB) else Color(0xFFE8F2FC),
                    RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    PinText(when { locked -> "Acceso temporalmente bloqueado"; incorrect -> "PIN incorrecto";
                        state.error != null -> "No pudimos completar el acceso"; else -> "Configura tu acceso" },
                        11, if (state.error != null) Color(0xFFAD2E3D) else Color(0xFF21457A), FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    PinText(when { locked -> "Se alcanzó el límite de intentos incorrectos. Intenta en 15 minutos.";
                        incorrect -> "Revisa los cuatro dígitos e intenta nuevamente.";
                        state.error == "NETWORK_UNAVAILABLE" -> "Revisa tu conexión e intenta nuevamente.";
                        state.error != null -> "Verifica tu vinculación e intenta nuevamente.";
                        else -> "Registra cuatro dígitos válidos para continuar." }, 9, tataMutedColor())
                }
            }
            Spacer(Modifier.height(10.dp))
            PinText("¿Olvidaste tu PIN? Pide ayuda a tu familiar.", 12, tataMutedColor(), modifier = Modifier.padding(start = 2.dp))
            TextButton(onClick = onBack) { PinText("Volver", 12, TataNavy) }
        }
    }
}

@Composable
private fun PinText(text: String, size: Int, color: Color, weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.bodySmall.copy(fontFamily = PinInter,
        fontSize = size.sp, lineHeight = (size * 1.3f).sp, letterSpacing = 0.sp, fontWeight = weight), color = color)
}
