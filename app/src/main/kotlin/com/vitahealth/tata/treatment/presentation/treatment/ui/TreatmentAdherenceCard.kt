package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.*
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel
import java.time.LocalDate

/**
 * Native seven-day chart; absent observations remain unfilled rather than implying zero adherence.
 */
@Composable
fun TreatmentAdherenceCard(
    evidence: TreatmentAdherenceReadModel?,
    error: String?,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    TataCard(modifier.fillMaxWidth(), containerColor = tataTonalColor(TataLavender)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Adherencia de este tratamiento",
                Modifier.weight(1f),
                color = tataTextColor(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            evidence?.let {
                Text(
                    "${it.percent}%",
                    color = tataAccentColor(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (evidence == null) {
            Text(
                error ?: "Sin tomas registradas en los últimos 7 días.",
                color = tataMutedColor(),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
            if (error != null && onRetry != null)
                TextButton(onClick = onRetry) { Text("Reintentar") }
        } else {
            val today = LocalDate.now()
            val points = evidence.trend.associateBy { it.date }
            val labels = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(7) { index ->
                    val date = today.minusDays(6L - index)
                    val percent = points[date]?.percent
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier.semantics(mergeDescendants = true) {
                                contentDescription =
                                    "$date: " +
                                        (percent?.let { "$it por ciento de adherencia" }
                                            ?: "sin registros")
                            },
                    ) {
                        Box(
                            Modifier.width(24.dp)
                                .height(58.dp)
                                .background(tataCardColor(), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (percent != null && percent > 0)
                                Box(
                                    Modifier.fillMaxWidth()
                                        .height((58f * percent / 100).dp)
                                        .background(tataAccentColor(), RoundedCornerShape(8.dp))
                                )
                        }
                        Text(
                            labels[date.dayOfWeek.value - 1],
                            color = tataMutedColor(),
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }
        }
    }
}
