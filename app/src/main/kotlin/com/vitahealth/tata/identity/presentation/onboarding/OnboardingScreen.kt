package com.vitahealth.tata.identity.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataMutedColor
import com.vitahealth.tata.shared.design.theme.tataTextColor

private val BrandBlue = Color(0xFF2B3F85)
private val BrandPurple = Color(0xFFA48DF0)
private val InterFont = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter))
private val SerifFont = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif))

/** The prototype frame is 393 dp wide; every position below is taken from it and scaled to the real width. */
private const val FRAME_WIDTH = 393f

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface),
    ) {
        Glows()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Hero()
            Spacer(Modifier.height(20.dp))
            Headline()
            Spacer(Modifier.height(24.dp))
            }
            TataButton(text = stringResource(R.string.onboarding_start), onClick = onStart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_have_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = tataMutedColor(),
                )
                TextButton(onClick = onSignIn, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_sign_in),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TataNavy,
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun Glows() {
    // Two soft colour washes behind the content, as in the prototype.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFDDE6FF).copy(alpha = 0.55f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(900f, 300f),
                    radius = 700f,
                ),
            ),
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFF8EBD9).copy(alpha = 0.45f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(0f, 1000f),
                    radius = 600f,
                ),
            ),
    )
}

@Composable
private fun Hero() {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory(scaleToDensity = true)) }.build()
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            // Only the title is announced; the sample cards are decoration.
            .clearAndSetSemantics { },
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
                model = "android.resource://" + context.packageName + "/" + R.raw.onboarding_butterfly,
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier
                    .padding(start = 4.dp, top = 8.dp)
                    .size(22.dp).rotate(20f),
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
            Text(text = "✓", color = Color(0xFF3F7F5E), fontSize = 18.sp, modifier = Modifier.align(Alignment.End))
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
            modifier = Modifier.offset(x = 246.dp.s(), y = 248.dp.s()).size(width = 66.dp, height = 78.dp).alpha(0.95f),
        )
    }
}

@Composable
private fun ReminderCard(
    time: String,
    medication: String,
    dose: String,
    container: Color,
    rotation: Float,
    modifier: Modifier = Modifier,
    titleSize: Int = 15,
    showToday: Boolean = false,
    decor: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .rotate(rotation)
            .shadow(8.dp, shape, ambientColor = Color(0x1A173B70), spotColor = Color(0x1A173B70))
            .background(Brush.horizontalGradient(listOf(container, when (container) {
                TataCream -> Color(0xFFFFEACF)
                TataLavender -> Color(0xFFE7DDFE)
                else -> Color(0xFFF6F6FC)
            })), shape)
            .height(108.dp)
            .padding(14.dp),
    ) {
        Column {
        Text(text = time, fontFamily = InterFont, fontSize = 11.sp, lineHeight = 14.sp, color = tataMutedColor())
        Text(
            text = medication,
            fontFamily = InterFont,
            fontWeight = FontWeight.Bold,
            fontSize = titleSize.sp,
            lineHeight = 18.sp,
            color = tataTextColor(),
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(text = dose, fontFamily = InterFont, fontSize = 12.sp, lineHeight = 15.sp, color = tataMutedColor(), modifier = Modifier.padding(top = 7.dp))
        }
        if (showToday) Row(Modifier.align(Alignment.BottomStart), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(Color.White, RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.onboarding_today), fontFamily = InterFont, fontSize = 10.sp,
                lineHeight = 12.sp, color = tataMutedColor())
        }
        Column(Modifier.align(Alignment.BottomEnd)) { decor() }
    }
}

@Composable
private fun Headline() {
    val title = buildAnnotatedString {
        append(stringResource(R.string.onboarding_title_1))
        append("\n")
        withStyle(SpanStyle(color = BrandPurple)) { append(stringResource(R.string.onboarding_title_2)) }
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
        modifier = Modifier
            .padding(top = 4.dp)
            .width(190.dp)
            .height(3.dp)
            .background(BrandPurple, RoundedCornerShape(2.dp)),
    )
    Text(
        text = stringResource(R.string.onboarding_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 14.dp),
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingScreenPreview() {
    TataTheme { OnboardingScreen(onStart = {}, onSignIn = {}) }
}
