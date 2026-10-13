package com.example

import com.example.ui.components.AdBannerState
import com.example.ui.components.AdMobConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdMobBannerTest {

    @Test
    fun testAdMobConstants_containValidSampleTestIds() {
        assertTrue(AdMobConstants.TEST_BANNER_ID.isNotEmpty())
        assertTrue(AdMobConstants.TEST_BANNER_ID.startsWith("ca-app-pub-"))
        assertTrue(AdMobConstants.TEST_APP_ID.contains("~"))
        assertEquals("ca-app-pub-3940256099942544/6300978111", AdMobConstants.TEST_BANNER_ID)
    }

    @Test
    fun testAdBannerState_errorProperties() {
        val errorState = AdBannerState.Error(errorCode = 3, errorMessage = "No fill")
        assertEquals(3, errorState.errorCode)
        assertEquals("No fill", errorState.errorMessage)
    }

    @Test
    fun testAdBannerState_transitions() {
        var state: AdBannerState = AdBannerState.Loading
        assertTrue(state is AdBannerState.Loading)

        state = AdBannerState.Loaded
        assertTrue(state is AdBannerState.Loaded)

        state = AdBannerState.Error(errorCode = 2, errorMessage = "Network error")
        assertTrue(state is AdBannerState.Error)
    }
}
