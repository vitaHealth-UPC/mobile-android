package com.vitahealth.tata.intake.presentation.detail

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val confirmedInter = FontFamily(Font(R.font.tata_inter))

@Composable
fun DoseConfirmedScreen(
    state: DoseDetailUiState.Content,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    onTab: ((AdultTab) -> Unit)? = null,
) {
    val alreadyConfirmed = state.outcome == ConfirmationOutcome.ALREADY_CONFIRMED
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val at = state.dose.confirmedAt?.atZone(ZoneId.systemDefault())
    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF)))
            )
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(if (alreadyConfirmed) 12.dp else 24.dp))
            Box(Modifier.fillMaxWidth().height(if (alreadyConfirmed) 132.dp else 148.dp)) {
                Box(
                    Modifier.align(Alignment.Center)
                        .size(96.dp)
                        .shadow(
                            10.dp,
                            CircleShape,
                            ambientColor = Color(0x1A1A2138),
                            spotColor = Color(0x1A1A2138),
                        )
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFCEEBD6), Color(0xFF8AC7A3))),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(48.dp)) {
                        drawLine(Color.White, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .75f), strokeWidth = 8.dp.toPx())
                        drawLine(Color.White, Offset(size.width * .42f, size.height * .75f), Offset(size.width * .86f, size.height * .25f), strokeWidth = 8.dp.toPx())
                    }
                }
                ConfirmationConfetti(Modifier.align(Alignment.TopCenter))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.confirmed_well_done),
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                fontSize = 30.sp,
                lineHeight = 36.sp,
                color = TataText,
            )
            Text(
                stringResource(if (state.outcome == ConfirmationOutcome.ALREADY_CONFIRMED) R.string.confirmed_already_title else R.string.confirmed_title),
                fontFamily = confirmedInter,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataNavy,
            )
            Spacer(Modifier.height(if (alreadyConfirmed) 24.dp else 38.dp))
            ConfirmedCard(Color.White) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TataSvgIcon(R.raw.dose_confirmed_calendar, Modifier.size(22.dp))
                    Spacer(Modifier.width(13.dp))
                    Column {
                        Text(
                            at?.format(DateTimeFormatter.ofPattern("d MMMM", locale))
                                ?: stringResource(R.string.confirmed_recorded),
                            fontFamily = confirmedInter,
                            fontSize = 13.sp,
                            color = TataMuted,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            at?.format(DateTimeFormatter.ofPattern("h:mm a", locale)).orEmpty(),
                            fontFamily = confirmedInter,
                            fontSize = 27.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TataText,
                        )
                    }
                }
            }
            state.nextDose?.let { next ->
                Spacer(Modifier.height(20.dp))
                ConfirmedCard(Color(0xFFECF8EF), Color(0xFFDDEFE1)) {
                    Box(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(end = 82.dp)) {
                            Text(
                                stringResource(R.string.confirmed_next),
                                fontFamily = confirmedInter,
                                fontSize = 12.sp,
                                color = TataMuted,
                            )
                            Spacer(Modifier.height(9.dp))
                            Text(
                                next.medicationName,
                                fontFamily = confirmedInter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                lineHeight = 22.sp,
                                color = TataText,
                            )
                            Text(
                                next.scheduledAt
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("d MMM, h:mm a", locale)),
                                fontFamily = confirmedInter,
                                fontSize = 13.sp,
                                color = TataMuted,
                            )
                        }
                        Box(Modifier.matchParentSize()) {
                            Image(painterResource(R.drawable.my_medications_tablet), null,
                                Modifier.align(Alignment.CenterEnd).size(102.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            ConfirmedCard(Color(0xFFF1EEFF), Color(0xFFE7E1FC)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TataSvgIcon(R.raw.dose_confirmed_family, Modifier.size(22.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.confirmed_progress),
                            fontFamily = confirmedInter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TataNavy,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            stringResource(R.string.confirmed_history),
                            fontFamily = confirmedInter,
                            fontSize = 13.sp,
                            color = TataMuted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            TataButton(stringResource(R.string.detail_back_home), onHome)
            if (state.outcome == ConfirmationOutcome.ALREADY_CONFIRMED) {
                Spacer(Modifier.height(18.dp))
                Column(Modifier.fillMaxWidth().background(TataLavender, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(stringResource(R.string.confirmed_no_duplicates), fontFamily = confirmedInter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, color = TataNavy)
                    Spacer(Modifier.height(7.dp))
                    Text(stringResource(R.string.confirmed_no_duplicates_detail), fontFamily = confirmedInter, fontSize = 8.5.sp, lineHeight = 12.sp, color = TataMuted)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        AdultTabBar(AdultTab.Home, { tab -> if (tab == AdultTab.Home) onHome() else onTab?.invoke(tab) },
            if (onTab != null) setOf(AdultTab.Home, AdultTab.Medications, AdultTab.Agenda, AdultTab.Notes) else setOf(AdultTab.Home))
    }
}

@Composable
private fun ConfirmedCard(first: Color, last: Color = first, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
            .background(Brush.horizontalGradient(listOf(first, last)), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        content()
    }
}

/** Small native decorations stay crisp on every density; Figma source bitmap is only 150 by 60. */
@Composable private fun ConfirmationConfetti(modifier: Modifier) {
    Box(modifier.width(96.dp).height(112.dp)) {
        Box(Modifier.offset(22.dp, 4.dp).size(5.dp, 12.dp).rotate(-28f).background(Color(0xFFD9C3EF), RoundedCornerShape(2.dp)))
        Box(Modifier.offset(65.dp, 1.dp).size(6.dp).background(Color(0xFFD3B9EA), CircleShape))
        Box(Modifier.offset(45.dp, 22.dp).size(6.dp, 12.dp).rotate(-25f).background(Color(0xFFFFE9A6), RoundedCornerShape(2.dp)))
        Box(Modifier.offset(26.dp, 36.dp).size(5.dp).background(Color(0xFFD5BEEB), CircleShape))
        Box(Modifier.offset(71.dp, 34.dp).size(6.dp, 12.dp).rotate(27f).background(Color(0xFFF1C0CB), RoundedCornerShape(2.dp)))
        Box(Modifier.offset(42.dp, 52.dp).size(7.dp).rotate(30f).background(Color(0xFF98D0BC), RoundedCornerShape(2.dp)))
        Box(Modifier.offset(63.dp, 68.dp).size(5.dp, 10.dp).rotate(25f).background(Color(0xFFD5C1EE), RoundedCornerShape(2.dp)))
        Box(Modifier.offset(13.dp, 76.dp).size(14.dp, 6.dp).rotate(18f).background(Color(0xFF82B69E), RoundedCornerShape(2.dp)))
    }
}
