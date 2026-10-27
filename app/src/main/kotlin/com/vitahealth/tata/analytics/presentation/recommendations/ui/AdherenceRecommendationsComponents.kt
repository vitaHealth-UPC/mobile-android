package com.vitahealth.tata.analytics.presentation.recommendations.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsUiState
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.RECOMMENDATIONS_PERIOD_DAYS
import com.vitahealth.tata.analytics.presentation.shared.ui.AnalyticsEmptyStateCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText

private val PatternCardStart = Color(0xFFF1ECFF)
private val PatternCardEnd = Color(0xFFECF4FB)
private val HeatmapLabel = Color(0xFF637087)
private val HeatCell = Color(0xFF0E2952)
private val BadgeColors = listOf(Color(0xFFE8F5EB), Color(0xFFFFF3E2), Color(0xFFECF4FB))
private val OutlinedActionColor = Color(0xFF21457A)

@Composable
internal fun AdherenceRecommendationsBody(
    state: AdherenceRecommendationsUiState,
    onBackToHistory: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.recommendations_title),
            fontFamily = FontFamily(Font(R.font.tata_serif)),
            fontSize = 29.sp,
            lineHeight = 36.sp,
            color = TataText,
        )
        Text(
            text = stringResource(R.string.recommendations_subtitle, RECOMMENDATIONS_PERIOD_DAYS),
            fontSize = 12.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(20.dp))

        when (state) {
            AdherenceRecommendationsUiState.Loading -> LoadingCard()
            is AdherenceRecommendationsUiState.Content -> RecommendationsContent(data = state.data)
            AdherenceRecommendationsUiState.InsufficientEvidence -> {
                Spacer(Modifier.height(75.dp))
                AnalyticsEmptyStateCard(
                    title = stringResource(R.string.recommendations_insufficient_title),
                    message = stringResource(R.string.recommendations_insufficient_body),
                )
                Spacer(Modifier.height(64.dp))
                OutlinedActionButton(
                    text = stringResource(R.string.recommendations_back),
                    onClick = onBackToHistory,
                )
            }
            is AdherenceRecommendationsUiState.Error ->
                ErrorCard(message = state.message, onRetry = onRetry)
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun RecommendationsContent(data: AdherenceRecommendationsUi) {
    PatternCard(data = data)
    Spacer(Modifier.height(20.dp))
    data.recommendations.forEachIndexed { index, recommendation ->
        RecommendationCard(
            number = index + 1,
            recommendation = recommendation,
            badgeColor = BadgeColors[index % BadgeColors.size],
        )
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun PatternCard(data: AdherenceRecommendationsUi) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(elevation = 3.dp, shape = shape)
                .background(
                    Brush.horizontalGradient(listOf(PatternCardStart, PatternCardEnd)),
                    shape,
                )
                .padding(16.dp)
    ) {
        Text(
            text = data.patternTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = data.patternSummary,
            fontSize = 12.sp,
            color = TataMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.recommendations_concentration),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = HeatmapLabel,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
            data.concentration.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { intensity ->
                        Box(
                            modifier =
                                Modifier.weight(1f)
                                    .height(7.dp)
                                    .background(
                                        HeatCell.copy(alpha = intensity),
                                        RoundedCornerShape(3.dp),
                                    )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(number: Int, recommendation: RecommendationUi, badgeColor: Color) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(elevation = 3.dp, shape = shape)
                .background(Color.White, shape)
                .border(BorderStroke(1.dp, TataBorder), shape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$number. ${recommendation.title}",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataText,
            )
            Text(
                text = recommendation.description,
                fontSize = 12.sp,
                color = TataMuted,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Box(
            modifier =
                Modifier.padding(start = 12.dp).size(34.dp).background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TataDeepNavy,
            )
        }
    }
}

@Composable
private fun OutlinedActionButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(25.dp)
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .background(Color.White, shape)
                .border(BorderStroke(1.dp, OutlinedActionColor), shape)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OutlinedActionColor,
        )
    }
}

@Composable
private fun LoadingCard() {
    TataCard(containerColor = TataLavender, modifier = Modifier.fillMaxWidth()) {
        Text("Recomendaciones", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Analizando tus patrones...",
            color = TataMuted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TataCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "No pudimos cargar las recomendaciones",
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = message, color = TataMuted, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(14.dp))
        TataButton(text = "Reintentar", onClick = onRetry, style = TataButtonStyle.Secondary)
    }
}
