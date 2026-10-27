package com.vitahealth.tata.carelink.presentation.link.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.carelink.presentation.link.ui.ConsentCard
import com.vitahealth.tata.carelink.presentation.link.ui.GradientCard
import com.vitahealth.tata.carelink.presentation.link.ui.LinkButton
import com.vitahealth.tata.carelink.presentation.link.ui.LinkInter
import com.vitahealth.tata.carelink.presentation.link.ui.LinkProgress
import com.vitahealth.tata.carelink.presentation.link.ui.LinkSerif
import com.vitahealth.tata.carelink.presentation.link.ui.LinkText
import com.vitahealth.tata.carelink.presentation.link.ui.OlderAdultCard
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkStep
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkUiState
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkViewModel
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import java.time.LocalDate
import java.time.Period
import java.util.Locale


@Composable
fun CareLinkScreen(
    state: CareLinkUiState,
    onCodeChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    onAcceptConsent: () -> Unit,
    onRejectConsent: () -> Unit,
    modifier: Modifier = Modifier,
    avatarResource: Int? = null,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(tataBackgroundColor())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 28.dp, bottom = 32.dp)
    ) {
        Text(
            "Vincula a tu familiar",
            fontFamily = LinkSerif,
            fontSize = 29.sp,
            lineHeight = 38.sp,
            color = tataTextColor(),
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        LinkText(
            "La relación de cuidado requiere consentimiento.",
            12,
            tataMutedColor(),
            modifier = Modifier.padding(top = 2.dp, start = 2.dp),
        )
        Spacer(Modifier.height(14.dp))
        LinkProgress(state.step)
        Spacer(Modifier.height(4.dp))
        LinkText("Código temporal", 11, tataMutedColor(), FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth()
                .height(58.dp)
                .tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp))
                .background(tataCardColor(), RoundedCornerShape(15.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = state.code,
                onValueChange = onCodeChange,
                enabled = state.step == CareLinkStep.Code && !state.isLoading,
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = LinkInter,
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        color = tataTextColor(),
                    ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (state.code.isEmpty()) LinkText("Ingresa tu código", 14, tataMutedColor())
                    inner()
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        OlderAdultCard(state.olderAdult, state.step, avatarResource)
        Spacer(Modifier.height(22.dp))
        LinkButton(
            if (state.isLoading && state.step == CareLinkStep.Code) "Enviando…"
            else "Enviar solicitud",
            onSendRequest,
            state.step == CareLinkStep.Code && !state.isLoading,
            Modifier.fillMaxWidth().height(56.dp),
        )
        Spacer(Modifier.height(20.dp))
        ConsentCard(state.olderAdult, state.step, state.isLoading, onAcceptConsent, onRejectConsent)
        Spacer(Modifier.height(26.dp))
        GradientCard(listOf(Color(0xFFEEF6FC), Color(0xFFE2EEF9))) {
            LinkText("Vínculo seguro y reversible", 13, tataTextColor(), FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LinkText(
                "El adulto puede retirar su consentimiento en cualquier momento.",
                11,
                tataMutedColor(),
            )
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
