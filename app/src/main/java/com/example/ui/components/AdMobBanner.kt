package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Standard Google AdMob test ad unit IDs.
 * Use these during development to avoid policy strikes against your production AdMob account.
 */
object AdMobConstants {
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
}

/**
 * Represents the current operational state of the AdMob banner.
 */
sealed class AdBannerState {
    data object Loading : AdBannerState()
    data object Loaded : AdBannerState()
    data class Error(val errorCode: Int, val errorMessage: String) : AdBannerState()
}

/**
 * Reusable AdMob Banner Composable with integrated error state management,
 * lifecycle synchronization, and accessibility test tags.
 *
 * @param adUnitId The AdMob Banner Unit ID. Defaults to Google's standard test banner unit.
 * @param modifier Custom modifier applied to the parent banner container.
 * @param adSize The AdSize for the banner (e.g., AdSize.BANNER, AdSize.LARGE_BANNER, AdSize.FULL_BANNER).
 * @param showLoadingPlaceholder When true, displays a minimal loading indicator while the ad is requested.
 * @param showErrorPlaceholder When true, renders a graceful fallback UI if ad loading fails.
 * @param enableRetry When true, provides a retry button on the error placeholder.
 * @param onAdLoaded Callback invoked when an ad has finished loading.
 * @param onAdFailedToLoad Callback invoked when an ad request fails, providing the LoadAdError.
 * @param onAdOpened Callback invoked when the user taps the ad and an overlay opens.
 * @param onAdClosed Callback invoked when the user returns from the ad overlay.
 * @param onAdClicked Callback invoked when an ad click is registered.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConstants.TEST_BANNER_ID,
    adSize: AdSize = AdSize.BANNER,
    showLoadingPlaceholder: Boolean = true,
    showErrorPlaceholder: Boolean = true,
    enableRetry: Boolean = true,
    onAdLoaded: () -> Unit = {},
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    onAdOpened: () -> Unit = {},
    onAdClosed: () -> Unit = {},
    onAdClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var bannerState by remember(adUnitId) { mutableStateOf<AdBannerState>(AdBannerState.Loading) }
    var retryTrigger by remember { mutableStateOf(0) }

    // Instantiate and configure the underlying Android AdView
    val adView = remember(adUnitId, adSize, retryTrigger) {
        AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(adUnitId)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    bannerState = AdBannerState.Loaded
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    bannerState = AdBannerState.Error(
                        errorCode = error.code,
                        errorMessage = error.message
                    )
                    onAdFailedToLoad(error)
                }

                override fun onAdOpened() {
                    onAdOpened()
                }

                override fun onAdClosed() {
                    onAdClosed()
                }

                override fun onAdClicked() {
                    onAdClicked()
                }
            }
            // Trigger the initial ad request
            loadAd(AdRequest.Builder().build())
        }
    }

    // Synchronize AdView lifecycle with Android Activity/LifecycleOwner to avoid memory leaks
    DisposableEffect(adView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("admob_banner_container"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = bannerState) {
                is AdBannerState.Loading -> {
                    if (showLoadingPlaceholder) {
                        AdLoadingPlaceholder(
                            adSize = adSize,
                            modifier = Modifier.testTag("admob_banner_loading")
                        )
                    }
                }

                is AdBannerState.Loaded -> {
                    // Loaded state: The native AdView is rendered
                }

                is AdBannerState.Error -> {
                    if (showErrorPlaceholder) {
                        AdErrorPlaceholder(
                            errorState = state,
                            enableRetry = enableRetry,
                            onRetry = {
                                bannerState = AdBannerState.Loading
                                retryTrigger++
                            },
                            modifier = Modifier.testTag("admob_banner_error")
                        )
                    }
                }
            }

            // Always embed the AndroidView so that it can complete loading and render into the layout
            AnimatedVisibility(
                visible = bannerState is AdBannerState.Loaded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier.testTag("admob_banner_view")
                )
            }
        }
    }
}

/**
 * Placeholder displayed while the banner ad is requesting from Google servers.
 */
@Composable
private fun AdLoadingPlaceholder(
    adSize: AdSize,
    modifier: Modifier = Modifier
) {
    val estimatedHeightDp = if (adSize.height > 0) adSize.height.dp else 50.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(estimatedHeightDp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Loading sponsored content…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Fallback card rendered when ad loading encounters an error or network limitation.
 * Presents a clear message and a retry affordance.
 */
@Composable
private fun AdErrorPlaceholder(
    errorState: AdBannerState.Error,
    enableRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val friendlyMessage = when (errorState.errorCode) {
        AdRequest.ERROR_CODE_NETWORK_ERROR -> "Network unavailable. Connect to the internet to load ads."
        AdRequest.ERROR_CODE_NO_FILL -> "Sponsored slot temporarily quiet (no fill)."
        AdRequest.ERROR_CODE_INVALID_REQUEST -> "Invalid ad request configuration."
        else -> "Sponsored banner unavailable (code ${errorState.errorCode})."
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Ad Info",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Sponsored Content",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = friendlyMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (enableRetry) {
            IconButton(
                onClick = onRetry,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("admob_banner_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry loading ad",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
