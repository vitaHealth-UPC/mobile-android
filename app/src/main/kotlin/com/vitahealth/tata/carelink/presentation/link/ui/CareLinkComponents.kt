package com.vitahealth.tata.carelink.presentation.link.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkStep
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import java.time.LocalDate
import java.time.Period
import java.util.Locale

internal val LinkInter = FontFamily(Font(R.font.tata_inter))
internal val LinkSerif = FontFamily(Font(R.font.tata_serif))
internal val LinkShape = RoundedCornerShape(18.dp)

@Composable
internal fun LinkText(
    text: String,
    size: Int,
    color: Color = tataTextColor(),
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier = modifier,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = LinkInter,
                fontSize = size.sp,
                lineHeight = (size * 1.3f).sp,
                letterSpacing = 0.sp,
                fontWeight = weight,
            ),
        color = color,
    )
}

@Composable
internal fun LinkProgress(step: CareLinkStep) {
    val context = LocalContext.current
    val imageLoader =
        remember(context) {
            ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
        }
    val activeIndex =
        when (step) {
            CareLinkStep.Code -> 0
            CareLinkStep.AwaitingConsent -> 1
            else -> 2
        }
    Box(
        Modifier.fillMaxWidth()
            .height(58.dp)
            .tataPrototypeShadow(8.dp, LinkShape)
            .background(tataCardColor(), LinkShape)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .padding(horizontal = 49.dp)
                .padding(top = 18.dp)
                .height(2.dp)
                .background(Color(0xFFDBE0ED))
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 10.dp)) {
            listOf("Código", "Solicitud", "Consentimiento").forEachIndexed { index, label ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                        val active = index <= activeIndex
                        val asset =
                            if (active) R.raw.figma_link_step_active
                            else R.raw.figma_link_step_inactive
                        AsyncImage(
                            model = "android.resource://${context.packageName}/$asset",
                            imageLoader = imageLoader,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            (index + 1).toString(),
                            fontFamily = LinkInter,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else TataDeepNavy,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinkText(
                        label,
                        9,
                        if (index == activeIndex) tataAccentColor(TataDeepNavy) else tataMutedColor(),
                        FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun OlderAdultCard(profile: OlderAdultProfile?, step: CareLinkStep, avatarResource: Int?) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 94.dp)
            .tataPrototypeShadow(8.dp, LinkShape)
            .background(tataCardColor(), LinkShape)
            .border(1.dp, Color(0xFFE8EAF1), LinkShape)
            .padding(horizontal = 13.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (avatarResource != null)
            Image(
                painterResource(avatarResource),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier.size(58.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape),
            )
        else
            Box(
                Modifier.size(58.dp).background(TataLavender, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val initials =
                    profile
                        ?.fullName
                        ?.split(' ')
                        ?.filter { it.isNotBlank() }
                        ?.take(2)
                        ?.joinToString("") { it.take(1).uppercase(Locale.forLanguageTag("es-419")) }
                        ?: "?"
                LinkText(initials, 20, TataDeepNavy, FontWeight.SemiBold)
            }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            val age =
                profile?.let {
                    Period.between(it.birthDate, LocalDate.now()).years.coerceAtLeast(0)
                }
            LinkText(
                profile?.let { "${it.fullName}, $age años" } ?: "Adulto mayor",
                16,
                tataTextColor(),
                FontWeight.SemiBold,
            )
            Spacer(Modifier.height(9.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinkText(
                    when (step) {
                        CareLinkStep.Code -> "Solicitud lista para enviar"
                        CareLinkStep.AwaitingConsent -> "Solicitud enviada"
                        CareLinkStep.Confirmed -> "Vínculo activo"
                        CareLinkStep.Rejected -> "Solicitud rechazada"
                    },
                    12,
                    tataMutedColor(),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.background(Color(0xFFFFF3E2), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    LinkText(
                        when (step) {
                            CareLinkStep.Confirmed -> "Activo"
                            CareLinkStep.Rejected -> "Rechazada"
                            else -> "Pendiente"
                        },
                        10,
                        Color(0xFFAB731F),
                        FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GradientCard(colors: List<Color>, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .tataPrototypeShadow(8.dp, LinkShape)
            .background(Brush.horizontalGradient(colors.map { tataTonalColor(it) }), LinkShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content,
    )
}

@Composable
internal fun ConsentCard(
    profile: OlderAdultProfile?,
    step: CareLinkStep,
    loading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    GradientCard(listOf(Color(0xFFF1ECFF), Color(0xFFE7DFFC))) {
        LinkText("Consentimiento del adulto mayor", 16, tataTextColor(), FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        val name = profile?.fullName?.substringBefore(' ') ?: "El adulto mayor"
        LinkText(
            when (step) {
                CareLinkStep.Rejected ->
                    "El consentimiento no fue autorizado. La información del adulto permanece protegida."
                CareLinkStep.Confirmed ->
                    "El vínculo fue autorizado para consultar la información de seguimiento."
                else ->
                    "$name autoriza a su familiar a consultar adherencia, alertas e información necesaria para su seguimiento."
            },
            12,
            tataMutedColor(),
        )
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            val enabled = step == CareLinkStep.AwaitingConsent && !loading
            LinkButton(
                if (loading && step == CareLinkStep.AwaitingConsent) "Procesando…"
                else "Aceptar vínculo",
                onAccept,
                enabled,
                Modifier.weight(1f).height(42.dp),
            )
            LinkButton(
                "Rechazar",
                onReject,
                enabled,
                Modifier.weight(1f).height(42.dp),
                secondary = true,
            )
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
internal fun LinkButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    secondary: Boolean = false,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.tataPrototypeShadow(6.dp, CircleShape),
        shape = CircleShape,
        shadowElevation = 0.dp,
        color = if (secondary) tataTonalColor(Color(0xFFF1ECFF)) else TataNavy,
        contentColor = if (secondary) tataAccentColor() else Color.White,
        border = if (secondary) BorderStroke(1.dp, tataOutlineColor(Color(0xFFC2B5E5))) else null,
    ) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                fontFamily = LinkInter,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
