package com.vitahealth.tata.intake.presentation.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun ConfirmedCard(first: Color, last: Color = first, content: @Composable () -> Unit) {
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
@Composable
internal fun ConfirmationConfetti(modifier: Modifier) {
    Box(modifier.width(96.dp).height(112.dp)) {
        Box(
            Modifier.offset(22.dp, 4.dp)
                .size(5.dp, 12.dp)
                .rotate(-28f)
                .background(Color(0xFFD9C3EF), RoundedCornerShape(2.dp))
        )
        Box(Modifier.offset(65.dp, 1.dp).size(6.dp).background(Color(0xFFD3B9EA), CircleShape))
        Box(
            Modifier.offset(45.dp, 22.dp)
                .size(6.dp, 12.dp)
                .rotate(-25f)
                .background(Color(0xFFFFE9A6), RoundedCornerShape(2.dp))
        )
        Box(Modifier.offset(26.dp, 36.dp).size(5.dp).background(Color(0xFFD5BEEB), CircleShape))
        Box(
            Modifier.offset(71.dp, 34.dp)
                .size(6.dp, 12.dp)
                .rotate(27f)
                .background(Color(0xFFF1C0CB), RoundedCornerShape(2.dp))
        )
        Box(
            Modifier.offset(42.dp, 52.dp)
                .size(7.dp)
                .rotate(30f)
                .background(Color(0xFF98D0BC), RoundedCornerShape(2.dp))
        )
        Box(
            Modifier.offset(63.dp, 68.dp)
                .size(5.dp, 10.dp)
                .rotate(25f)
                .background(Color(0xFFD5C1EE), RoundedCornerShape(2.dp))
        )
        Box(
            Modifier.offset(13.dp, 76.dp)
                .size(14.dp, 6.dp)
                .rotate(18f)
                .background(Color(0xFF82B69E), RoundedCornerShape(2.dp))
        )
    }
}
