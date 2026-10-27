package com.vitahealth.tata.identity.presentation.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
/** Figma reference width used to scale decorative positions without changing layout. */
private const val FRAME_WIDTH = 393f

internal val BrandBlue = Color(0xFF2B3F85)
internal val BrandPurple = Color(0xFFA48DF0)
internal val InterFont = FontFamily(Font(R.font.tata_inter))
internal val SerifFont = FontFamily(Font(R.font.tata_serif))

@Composable
internal fun Glows() {
    // Two soft colour washes behind the content, as in the prototype.
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFDDE6FF).copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(900f, 300f),
                        radius = 700f,
                    )
                )
    )
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF8EBD9).copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(0f, 1000f),
                        radius = 600f,
                    )
                )
    )
}

@Composable
internal fun Hero() {
    val context = LocalContext.current
    val imageLoader =
        remember(context) {
            ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory(scaleToDensity = true)) }
                .build()
        }
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth()
                .height(290.dp)
                // Only the title is announced; the sample cards are decoration.
                .clearAndSetSemantics {}
    ) {
        val scale = maxWidth.value / (FRAME_WIDTH - 48f)
        fun Dp.s(): Dp = this * scale

        Row(modifier = Modifier.offset(x = 4.dp, y = 0.dp), verticalAlignment = Alignment.Top) {
            Text(
                text = "Tata",
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 46.sp,
                color = BrandBlue,
            )
            AsyncImage(
                model =
                    "android.resource://" + context.packageName + "/" + R.raw.onboarding_butterfly,
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp).size(22.dp).rotate(20f),
            )
        }

        ReminderCard(
            time = stringResource(R.string.onboarding_card_a_time),
            medication = stringResource(R.string.onboarding_card_a_name),
            dose = stringResource(R.string.onboarding_card_a_dose),
            container = TataCream,
            rotation = 8f,
            showToday = true,
            modifier = Modifier.offset(x = 14.dp.s(), y = 89.dp.s()).width(146.dp.s()),
        ) {
            TataSvgIcon(
                resource = R.raw.onboarding_sun,
                modifier = Modifier.size(34.dp).align(Alignment.End),
            )
        }
        ReminderCard(
            time = stringResource(R.string.onboarding_card_b_time),
            medication = stringResource(R.string.onboarding_card_b_name),
            dose = stringResource(R.string.onboarding_card_b_dose),
            container = TataLavender,
            rotation = -7f,
            titleSize = 12,
            modifier = Modifier.offset(x = 178.dp.s(), y = 93.dp.s()).width(145.dp.s()),
        ) {
            Text(
                text = "✓",
                color = Color(0xFF3F7F5E),
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.End),
            )
        }
        ReminderCard(
            time = stringResource(R.string.onboarding_card_c_time),
            medication = stringResource(R.string.onboarding_card_c_name),
            dose = stringResource(R.string.onboarding_card_c_dose),
            container = Color.White,
            rotation = -3f,
            modifier = Modifier.offset(x = 93.dp.s(), y = 197.dp.s()).width(148.dp.s()),
        ) {}
        TataSvgIcon(
            resource = R.raw.onboarding_heart,
            modifier = Modifier.offset(x = 284.dp.s(), y = 93.dp.s()).size(30.dp),
        )
        TataSvgIcon(
            resource = R.raw.onboarding_leaf,
            modifier =
                Modifier.offset(x = 246.dp.s(), y = 248.dp.s())
                    .size(width = 66.dp, height = 78.dp)
                    .alpha(0.95f),
        )
    }
}

@Composable
internal fun ReminderCard(
    time: String,
    medication: String,
    dose: String,
    container: Color,
    rotation: Float,
    modifier: Modifier = Modifier,
    titleSize: Int = 15,
    showToday: Boolean = false,
    decor: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier =
            modifier
                .rotate(rotation)
                .shadow(
                    8.dp,
                    shape,
                    ambientColor = Color(0x1A173B70),
                    spotColor = Color(0x1A173B70),
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            container,
                            when (container) {
                                TataCream -> Color(0xFFFFEACF)
                                TataLavender -> Color(0xFFE7DDFE)
                                else -> Color(0xFFF6F6FC)
                            },
                        )
                    ),
                    shape,
                )
                .height(108.dp)
                .padding(14.dp)
    ) {
        Column {
            Text(
                text = time,
                fontFamily = InterFont,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = tataMutedColor(),
            )
            Text(
                text = medication,
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize.sp,
                lineHeight = 18.sp,
                color = tataTextColor(),
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = dose,
                fontFamily = InterFont,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = tataMutedColor(),
                modifier = Modifier.padding(top = 7.dp),
            )
        }
        if (showToday)
            Row(
                Modifier.align(Alignment.BottomStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(10.dp).background(Color.White, RoundedCornerShape(50)))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.onboarding_today),
                    fontFamily = InterFont,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = tataMutedColor(),
                )
            }
        Column(Modifier.align(Alignment.BottomEnd)) { decor() }
    }
}

@Composable
internal fun Headline() {
    val title = buildAnnotatedString {
        append(stringResource(R.string.onboarding_title_1))
        append("\n")
        withStyle(SpanStyle(color = BrandPurple)) {
            append(stringResource(R.string.onboarding_title_2))
        }
        append("\n")
        append(stringResource(R.string.onboarding_title_3))
    }
    Text(
        text = title,
        fontFamily = SerifFont,
        fontSize = 34.sp,
        lineHeight = 44.sp,
        color = tataTextColor(),
        modifier = Modifier.semantics { heading() },
    )
    Box(
        modifier =
            Modifier.padding(top = 4.dp)
                .width(190.dp)
                .height(3.dp)
                .background(BrandPurple, RoundedCornerShape(2.dp))
    )
    Text(
        text = stringResource(R.string.onboarding_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 14.dp),
    )
}
