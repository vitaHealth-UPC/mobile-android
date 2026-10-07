package com.vitahealth.tata.shared.design.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.svg.SvgDecoder

/** Local Figma vector asset; controls and layouts remain native Compose. */
@Composable
fun TataSvgIcon(@RawRes resource: Int, modifier: Modifier = Modifier, colorFilter: ColorFilter? = null) {
    val context = LocalContext.current
    val loader = remember(context) {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    }
    AsyncImage(
        model = "android.resource://${context.packageName}/$resource",
        imageLoader = loader,
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}
