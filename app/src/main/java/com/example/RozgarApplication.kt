package com.example

import android.app.ActivityManager
import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * RozgarApplication
 * Application entry-point providing a globally optimized ImageLoader configuration
 * for Coil across the entire Rozgar Kashmir ecosystem.
 *
 * Optimizations include:
 * - 25% app memory allocation for MemoryCache with strong reference tracking
 * - 150 MB dedicated disk cache with custom directory
 * - Smooth 350ms crossfade transitions for pleasant visual reveals
 * - Aggressive caching policies (CachePolicy.ENABLED) for mobile stability and offline access
 * - Automatic hardware bitmap acceleration on Android O+
 * - Adaptive RGB_565 configuration on low-RAM devices to prevent OutOfMemory crashes
 *   when scrolling through high-res product photos
 */
class RozgarApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        com.example.util.LocalizationManager.initialize(this)
        try {
            com.google.android.gms.ads.MobileAds.initialize(this) {}
        } catch (e: Exception) {
            android.util.Log.w("RozgarApplication", "AdMob initialization deferred/failed: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val isLowRam = isLowRamDevice()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            // Memory cache: 25% of available RAM to handle multi-photo product galleries smoothly
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            // Disk cache: 150 MB in cache directory to prevent re-fetching over Kashmir cellular networks
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_product_cache"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            // Smooth crossfade animation
            .crossfade(true)
            .crossfade(350)
            // Mobile caching policies
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Ensure reliable offline/cached photo availability
            // Hardware acceleration & low-RAM protection
            .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            .allowRgb565(isLowRam)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }

    private fun isLowRamDevice(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as? ActivityManager
        return am?.isLowRamDevice == true
    }
}
