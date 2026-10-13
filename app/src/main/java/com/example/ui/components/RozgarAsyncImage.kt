package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.ui.theme.KashmirEmerald
import com.example.ui.theme.KashmirSurfaceVariant
import com.example.ui.theme.KashmirTextMuted

/**
 * RozgarAsyncImage
 * Composable wrapper around Coil's SubcomposeAsyncImage configured for high-performance
 * photo loading in Rozgar Kashmir.
 *
 * Features:
 * - Shimmer skeleton placeholder while loading
 * - Graceful fallback on broken or missing image links
 * - Smooth crossfade animation
 * - Respects global memory and disk caching policies
 */
@Composable
fun RozgarAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentScale: ContentScale = ContentScale.Crop,
    defaultPlaceholderIcon: ImageVector = Icons.Default.Image,
    crossfadeDurationMillis: Int = 300
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(model)
        .crossfade(crossfadeDurationMillis)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()

    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.clip(shape),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmerEffect(shape = shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = defaultPlaceholderIcon,
                    contentDescription = null,
                    tint = KashmirEmerald.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KashmirSurfaceVariant, shape = shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = KashmirTextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    )
}
