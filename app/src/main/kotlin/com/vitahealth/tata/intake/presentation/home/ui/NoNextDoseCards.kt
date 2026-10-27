package com.vitahealth.tata.intake.presentation.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor

/** Same home-card hierarchy without carrying a previous dose into an empty server result. */
@Composable
internal fun NoNextDoseCard() {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth()
            .tataPrototypeShadow(6.dp, shape)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(tataTonalColor(Color(0xFFEEE8FF)), tataTonalColor(Color(0xFFE5DDF8)))
                )
            )
            .heightIn(min = 165.dp)
            .padding(start = 18.dp, end = 14.dp, top = 18.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.home_empty_title),
                fontFamily = homeInter,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = tataAccentColor(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.home_empty_description),
                fontFamily = homeInter,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = tataTextColor(),
            )
        }
        Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
            Image(
                painterResource(R.drawable.home_tablet),
                null,
                Modifier.requiredSize(176.dp).offset(y = 12.dp),
            )
        }
    }
}

/** Empty pending list is distinct from a completed daily adherence percentage. */
@Composable
internal fun NoNextDoseStatus(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .background(tataTonalColor(Color(0xFFE8F5EB)), RoundedCornerShape(16.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            stringResource(R.string.home_empty_status_title),
            color = tataAccentColor(Color(0xFF2E6E45)),
            fontFamily = homeInter,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.home_empty_status_description),
            color = tataTextColor(),
            fontFamily = homeInter,
            fontSize = 9.sp,
            lineHeight = 12.sp,
        )
    }
}
