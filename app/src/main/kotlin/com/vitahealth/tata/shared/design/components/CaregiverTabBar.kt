package com.vitahealth.tata.shared.design.components

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy

enum class CaregiverTab(val label: String, @RawRes val icon: Int) {
    Home("Inicio", R.raw.caregiver_tab_home),
    Alerts("Alertas", R.raw.caregiver_tab_alerts),
    Notes("Notas", R.raw.caregiver_tab_notes),
    Person("Persona", R.raw.caregiver_tab_person),
    More("Más", R.raw.caregiver_tab_more),
}

/** Bottom tab bar of the caregiver app, as drawn in the Figma "System / Tab Bar". */
@Composable
fun CaregiverTabBar(
    selected: CaregiverTab,
    onSelect: (CaregiverTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x141A2138), spotColor = Color(0x141A2138))
            .background(Color.White, RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CaregiverTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Tab, onClick = { onSelect(tab) })
                    .semantics { this.selected = isSelected },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp, 30.dp)
                        .background(if (isSelected) TataNavy else Color.Transparent, RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    TataSvgIcon(tab.icon, Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(if (isSelected) Color.White else TataMuted))
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = tab.label,
                    fontFamily = FontFamily(Font(R.font.tata_inter)),
                    fontSize = 8.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) TataNavy else TataMuted,
                )
            }
        }
    }
}
