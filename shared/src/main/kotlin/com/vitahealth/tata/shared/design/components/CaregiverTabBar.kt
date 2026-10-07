package com.vitahealth.tata.shared.design.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.shared.R
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy

enum class CaregiverTab(val label: String, @DrawableRes val icon: Int) {
    Home("Inicio", R.drawable.tab_home),
    Alerts("Alertas", R.drawable.tab_alerts),
    Notes("Notas", R.drawable.tab_notes),
    Person("Persona", R.drawable.tab_person),
    More("Más", R.drawable.tab_more),
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
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(28.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        CaregiverTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column(
                modifier = Modifier
                    .widthIn(min = 48.dp)
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Tab, onClick = { onSelect(tab) }),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp, 30.dp)
                        .background(if (isSelected) TataNavy else Color.Transparent, RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(tab.icon), contentDescription = null, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = tab.label,
                    fontSize = 9.sp,
                    color = if (isSelected) TataNavy else TataMuted,
                )
            }
        }
    }
}
