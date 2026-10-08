package com.vitahealth.tata.carelink.presentation.link

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.carelink.domain.model.OlderAdultProfile
import com.vitahealth.tata.shared.design.theme.*
import java.time.LocalDate
import java.time.Period

private val LinkInter = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter))
private val LinkSerif = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif))
private val LinkShape = RoundedCornerShape(18.dp)

@Composable
fun CareLinkRoute(factory: CareLinkViewModel.Factory, modifier: Modifier = Modifier,
    onConfirmed: (olderAdultId: String, olderAdultName: String) -> Unit = { _, _ -> },
    initialCode: String = "", initialOlderAdultId: String = "") {
    val vm: CareLinkViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(initialCode) { if (initialCode.isNotBlank()) vm.onCodeChange(initialCode) }
    LaunchedEffect(initialOlderAdultId) { if (initialOlderAdultId.isNotBlank()) vm.previewOlderAdult(initialOlderAdultId) }
    LaunchedEffect(state.step, state.acceptedLink?.id, state.olderAdult?.id) {
        val link = state.acceptedLink
        val adult = state.olderAdult
        if (state.step == CareLinkStep.Confirmed && link != null && adult != null) onConfirmed(link.olderAdultId, adult.fullName)
    }
    CareLinkScreen(state, vm::onCodeChange, vm::sendLinkRequest, vm::acceptConsent, vm::rejectConsent, modifier)
}

@Composable
fun CareLinkScreen(state: CareLinkUiState, onCodeChange: (String) -> Unit,
    onSendRequest: () -> Unit, onAcceptConsent: () -> Unit, onRejectConsent: () -> Unit,
    modifier: Modifier = Modifier, avatarResource: Int? = null) {
    Column(modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp).padding(top = 28.dp, bottom = 32.dp)) {
        Text("Vincula a tu familiar", fontFamily = LinkSerif, fontSize = 29.sp,
            lineHeight = 38.sp, color = tataTextColor(), modifier = Modifier.padding(horizontal = 2.dp))
        LinkText("La relación de cuidado requiere consentimiento.", 12, tataMutedColor(),
            modifier = Modifier.padding(top = 2.dp, start = 2.dp))
        Spacer(Modifier.height(14.dp))
        LinkProgress(state.step)
        Spacer(Modifier.height(4.dp))
        LinkText("Código temporal", 11, tataMutedColor(), FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(58.dp).tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp))
            .background(Color.White, RoundedCornerShape(15.dp)).padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart) {
            BasicTextField(value = state.code, onValueChange = onCodeChange,
                enabled = state.step == CareLinkStep.Code && !state.isLoading, singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = LinkInter,
                    fontSize = 14.sp, letterSpacing = 0.sp, color = tataTextColor()),
                modifier = Modifier.fillMaxWidth(), decorationBox = { inner ->
                    if (state.code.isEmpty()) LinkText("Ingresa tu código", 14, tataMutedColor())
                    inner()
                })
        }
        Spacer(Modifier.height(10.dp))
        OlderAdultCard(state.olderAdult, state.step, avatarResource)
        Spacer(Modifier.height(22.dp))
        LinkButton(if (state.isLoading && state.step == CareLinkStep.Code) "Enviando…" else "Enviar solicitud",
            onSendRequest, state.step == CareLinkStep.Code && !state.isLoading,
            Modifier.fillMaxWidth().height(56.dp))
        Spacer(Modifier.height(20.dp))
        ConsentCard(state.olderAdult, state.step, state.isLoading, onAcceptConsent, onRejectConsent)
        Spacer(Modifier.height(26.dp))
        GradientCard(listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))) {
            LinkText("Vínculo seguro y reversible", 13, tataTextColor(), FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LinkText("El adulto puede retirar su consentimiento en cualquier momento.", 11, tataMutedColor())
        }
        state.errorMessage?.let {
            Spacer(Modifier.height(10.dp))
            GradientCard(listOf(TataErrorSurface, TataErrorSurface)) {
                LinkText("No se pudo vincular", 11, TataError, FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                LinkText(it, 10, tataMutedColor())
            }
        }
    }
}

@Composable
private fun LinkText(text: String, size: Int, color: Color = tataTextColor(),
    weight: FontWeight = FontWeight.Normal, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.bodySmall.copy(
        fontFamily = LinkInter, fontSize = size.sp, lineHeight = (size * 1.3f).sp,
        letterSpacing = 0.sp, fontWeight = weight), color = color)
}

@Composable
private fun LinkProgress(step: CareLinkStep) {
    val context = LocalContext.current
    val imageLoader = remember(context) { ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build() }
    val activeIndex = when (step) { CareLinkStep.Code -> 0; CareLinkStep.AwaitingConsent -> 1; else -> 2 }
    Box(Modifier.fillMaxWidth().height(58.dp).tataPrototypeShadow(8.dp, LinkShape).background(Color.White, LinkShape)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 49.dp).padding(top = 18.dp)
            .height(2.dp).background(Color(0xFFDBE0ED)))
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 10.dp)) {
            listOf("Código", "Solicitud", "Consentimiento").forEachIndexed { index, label ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                        val active = index <= activeIndex
                        val asset = if (active) R.raw.figma_link_step_active else R.raw.figma_link_step_inactive
                        AsyncImage(model = "android.resource://${context.packageName}/$asset",
                            imageLoader = imageLoader, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text((index + 1).toString(), fontFamily = LinkInter, fontSize = 9.sp,
                            lineHeight = 12.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else TataDeepNavy)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinkText(label, 9, if(index == activeIndex) TataDeepNavy else tataMutedColor(), FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun OlderAdultCard(profile: OlderAdultProfile?, step: CareLinkStep, avatarResource: Int?) {
    Row(Modifier.fillMaxWidth().heightIn(min = 94.dp).tataPrototypeShadow(8.dp, LinkShape)
        .background(Color.White, LinkShape).border(1.dp, Color(0xFFE8EAF1), LinkShape)
        .padding(horizontal = 13.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        if (avatarResource != null) Image(painterResource(avatarResource), contentDescription = null,
            contentScale = ContentScale.Crop, modifier = Modifier.size(58.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape))
        else Box(Modifier.size(58.dp).background(TataLavender, CircleShape), contentAlignment = Alignment.Center) {
            val initials = profile?.fullName?.split(' ')?.filter { it.isNotBlank() }?.take(2)
                ?.joinToString("") { it.take(1).uppercase(java.util.Locale.forLanguageTag("es-419")) } ?: "?"
            LinkText(initials, 20, TataDeepNavy, FontWeight.SemiBold)
        }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            val age = profile?.let { Period.between(it.birthDate, LocalDate.now()).years.coerceAtLeast(0) }
            LinkText(profile?.let { "${it.fullName}, $age años" } ?: "Adulto mayor", 16, tataTextColor(), FontWeight.SemiBold)
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinkText(when(step) { CareLinkStep.Code -> "Solicitud lista para enviar"; CareLinkStep.AwaitingConsent -> "Solicitud enviada";
                    CareLinkStep.Confirmed -> "Vínculo activo"; CareLinkStep.Rejected -> "Solicitud rechazada" }, 12, tataMutedColor(), modifier = Modifier.weight(1f))
                Box(Modifier.background(Color(0xFFFFF3E2), CircleShape).padding(horizontal = 14.dp, vertical = 5.dp)) {
                    LinkText(when (step) { CareLinkStep.Confirmed -> "Activo"; CareLinkStep.Rejected -> "Rechazada"; else -> "Pendiente" }, 10, Color(0xFFAB731F), FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun GradientCard(colors: List<Color>, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, LinkShape).background(Brush.horizontalGradient(colors), LinkShape)
        .padding(horizontal = 16.dp, vertical = 14.dp), content = content)
}

@Composable
private fun ConsentCard(profile: OlderAdultProfile?, step: CareLinkStep, loading: Boolean, onAccept: () -> Unit, onReject: () -> Unit) {
    GradientCard(listOf(Color(0xFFF1ECFF), Color(0xFFE7DFFC))) {
        LinkText("Consentimiento del adulto mayor", 16, tataTextColor(), FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val name = profile?.fullName?.substringBefore(' ') ?: "El adulto mayor"
        LinkText(when (step) {
            CareLinkStep.Rejected -> "El consentimiento no fue autorizado. La información del adulto permanece protegida."
            CareLinkStep.Confirmed -> "El vínculo fue autorizado para consultar la información de seguimiento."
            else -> "$name autoriza a su familiar a consultar adherencia, alertas e información necesaria para su seguimiento."
        }, 12, tataMutedColor())
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            val enabled = step == CareLinkStep.AwaitingConsent && !loading
            LinkButton(if (loading && step == CareLinkStep.AwaitingConsent) "Procesando…" else "Aceptar vínculo", onAccept,
                enabled, Modifier.weight(1f).height(42.dp))
            LinkButton("Rechazar", onReject, enabled, Modifier.weight(1f).height(42.dp), secondary = true)
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun LinkButton(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier, secondary: Boolean = false) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.tataPrototypeShadow(6.dp, CircleShape),
        shape = CircleShape, shadowElevation = 0.dp,
        color = if (secondary) Color(0xFFF1ECFF) else TataNavy,
        contentColor = if (secondary) TataNavy else Color.White,
        border = if (secondary) BorderStroke(1.dp, Color(0xFFC2B5E5)) else null) {
        Box(Modifier.fillMaxSize().padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
            Text(text, fontFamily = LinkInter, fontSize = 15.sp, lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}
