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
import coil3.request.ImageRequest
import coil3.size.Size

/** Local Figma vector asset; controls and layouts remain native Compose. */
@Composable
fun TataSvgIcon(@RawRes resource: Int, modifier: Modifier = Modifier, colorFilter: ColorFilter? = null) {
    val context = LocalContext.current
    val loader = remember(context) {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory(scaleToDensity = true)) }.build()
    }
    val request = remember(context, resource) {
        ImageRequest.Builder(context).data("android.resource://${context.packageName}/$resource")
            .size(Size.ORIGINAL).build()
    }
    AsyncImage(
        model = request,
        imageLoader = loader,
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}
