package com.vitahealth.tata.shared.design.components

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy

enum class AdultTab(@StringRes val label: Int, @RawRes val icon: Int) {
    Home(R.string.adult_tab_home, R.raw.adult_tab_home),
    Medications(R.string.adult_tab_medications, R.raw.adult_tab_medications),
    Agenda(R.string.adult_tab_agenda, R.raw.adult_tab_agenda),
    Notes(R.string.adult_tab_notes, R.raw.adult_tab_notes),
    More(R.string.adult_tab_more, R.raw.adult_tab_more),
}

/** Figma adult navigation. Callers enable only destinations they can actually open. */
@Composable
fun AdultTabBar(
    selected: AdultTab,
    onSelect: (AdultTab) -> Unit,
    availableTabs: Set<AdultTab>,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138)).background(Color.White, shape).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AdultTab.entries.forEach { tab ->
            val active = tab == selected
            Column(
                Modifier.weight(1f).heightIn(min = 48.dp)
                    .clickable(enabled = tab in availableTabs, role = Role.Tab) { onSelect(tab) }
                    .semantics { this.selected = active },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(38.dp, 30.dp).background(if (active) TataNavy else Color.Transparent, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                    val iconModifier = if (tab == AdultTab.Medications) Modifier.size(14.4.dp, 9.6.dp).rotate(-42f) else Modifier.size(22.dp)
                    TataSvgIcon(tab.icon, iconModifier,
                        colorFilter = ColorFilter.tint(if (active) Color.White else TataMuted))
                }
                Spacer(Modifier.height(5.dp))
                Text(stringResource(tab.label), fontFamily = FontFamily(Font(R.font.tata_inter)),
                    fontSize = 8.5.sp, lineHeight = 12.sp, color = if (active) TataNavy else TataMuted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}
