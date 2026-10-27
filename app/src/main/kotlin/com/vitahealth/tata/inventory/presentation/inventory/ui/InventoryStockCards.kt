package com.vitahealth.tata.inventory.presentation.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val AvailableGreen = Color(0xFF2E754A)

private val batchDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.forLanguageTag("es-419"))
        .withZone(ZoneId.systemDefault())

/** Displays the server-authoritative stock and its coverage estimate. */
@Composable
internal fun StockCard(state: InventoryUiState.Ready) {
    val stock = state.stock
    val isLow = stock.status == StockStatus.LOW
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
    ) {
        Box(
            Modifier.fillMaxWidth()
                .heightIn(min = 150.dp)
                .background(
                    if (LocalTataAccessibility.current.darkTheme) {
                        Brush.horizontalGradient(listOf(tataCardColor(), tataCardColor()))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFFFF3E2), Color(0xFFFBE8C7)))
                    },
                )
        ) {
            Column(
                Modifier.padding(start = 16.dp, top = 16.dp, bottom = 18.dp).widthIn(max = 190.dp)
            ) {
                InventoryText(state.medicationName, 17, tataTextColor(), FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                InventoryText(
                    if (state.unit.isBlank())
                        stringResource(
                            R.string.inventory_units_remaining_no_unit,
                            stock.remainingStock,
                        )
                    else
                        stringResource(
                            R.string.inventory_units_remaining,
                            stock.remainingStock,
                            state.unit,
                        ),
                    18,
                    tataTextColor(),
                    FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                InventoryText(
                    stock.daysRemaining?.let {
                        stringResource(R.string.inventory_days_estimate, it)
                    } ?: stringResource(R.string.inventory_days_no_estimate),
                    12,
                    tataMutedColor(),
                )
            }
            Column(
                Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                InventoryText(
                    stringResource(
                        if (isLow) R.string.inventory_status_low
                        else R.string.inventory_status_available
                    ),
                    12,
                    if (LocalTataAccessibility.current.darkTheme) {
                        if (isLow) MaterialTheme.colorScheme.error else Color(0xFFB8E6C7)
                    } else if (isLow) Color(0xFFD94759) else AvailableGreen,
                    FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                // Decorative ring from the mockup; both stock and day estimate come from the API.
                Box(
                    Modifier.size(78.dp).border(9.dp, Color(0xFFE1AE4B), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        Modifier.size(54.dp).background(tataCardColor(), CircleShape),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        InventoryText(
                            stock.daysRemaining?.toString() ?: "—",
                            20,
                            tataAccentColor(TataDeepNavy),
                            FontWeight.Bold,
                        )
                        InventoryText(stringResource(R.string.inventory_days_short), 9, tataMutedColor())
                    }
                }
            }
        }
    }
}

/** Displays the latest batch returned by the inventory projection. */
@Composable
internal fun LastReplenishmentCard(batch: InventoryBatch, unit: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
    ) {
        Box(
            Modifier.fillMaxWidth()
                .heightIn(min = 92.dp)
                .background(
                    if (LocalTataAccessibility.current.darkTheme) {
                        Brush.horizontalGradient(listOf(tataCardColor(), tataCardColor()))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFECF4FB), Color(0xFFDFECF6)))
                    },
                )
        ) {
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 64.dp, bottom = 12.dp)) {
                InventoryText(
                    stringResource(R.string.inventory_last_replenishment),
                    12,
                    tataTextColor(),
                    FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                InventoryText(
                    batchDateFormatter.format(batch.registeredAt),
                    15,
                    tataTextColor(),
                    FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                val quantity =
                    if (unit.isBlank())
                        stringResource(R.string.inventory_batch_summary_no_unit, batch.quantity)
                    else stringResource(R.string.inventory_batch_summary, batch.quantity, unit)
                InventoryText(
                    listOfNotNull(quantity, batch.lot?.takeIf { it.isNotBlank() })
                        .joinToString(", "),
                    11,
                    tataMutedColor(),
                )
            }
            Box(
                Modifier.align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 17.dp)
                    .background(tataTonalColor(TataMint), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                InventoryText(
                    stringResource(R.string.inventory_registered_badge),
                    10,
                    if (LocalTataAccessibility.current.darkTheme) Color(0xFFB8E6C7) else AvailableGreen,
                )
            }
            val context = LocalContext.current
            coil3.compose.AsyncImage(
                model =
                    coil3.request.ImageRequest.Builder(context)
                        .data(R.raw.figma_inventory_medication)
                        .decoderFactory(coil3.svg.SvgDecoder.Factory())
                        .build(),
                contentDescription = null,
                colorFilter =
                    if (LocalTataAccessibility.current.darkTheme) {
                        ColorFilter.tint(tataAccentColor(TataDeepNavy))
                    } else {
                        null
                    },
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(end = 27.dp, bottom = 18.dp)
                        .size(24.dp),
            )
        }
    }
}
