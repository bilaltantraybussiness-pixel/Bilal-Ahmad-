package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class ResolvedAddress(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String,
    val houseOrBuilding: String = "",
    val street: String = "",
    val locality: String = "",
    val landmark: String = "",
    val district: String = "Srinagar",
    val state: String = "Jammu & Kashmir",
    val pincode: String = "190001"
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): ResolvedAddress? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null

        var location: Location? = null

        // 1. Try Google Play Services Location first
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            val task = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            val result = com.google.android.gms.tasks.Tasks.await(task, 4, java.util.concurrent.TimeUnit.SECONDS)
            if (result != null) {
                location = result
            }
        } catch (_: Exception) {
            // Fallback to LocationManager
        }

        // 2. Fallback to System LocationManager
        if (location == null) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (lm != null) {
                    val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val passiveLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    location = gpsLoc ?: netLoc ?: passiveLoc
                }
            } catch (_: Exception) {}
        }

        // Default to central Srinagar Lal Chowk if no coordinates acquired
        val lat = location?.latitude ?: 34.0837
        val lng = location?.longitude ?: 74.7973

        return@withContext reverseGeocode(context, lat, lng)
    }

    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): ResolvedAddress =
        withContext(Dispatchers.IO) {
            var formatted = "Lal Chowk, Srinagar, Jammu & Kashmir 190001"
            var house = ""
            var street = ""
            var locality = "Lal Chowk"
            var district = "Srinagar"
            var state = "Jammu & Kashmir"
            var pin = "190001"

            try {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = ArrayList<Address>()
                    val latch = java.util.concurrent.CountDownLatch(1)
                    geocoder.getFromLocation(latitude, longitude, 1) { list ->
                        addresses.addAll(list)
                        latch.countDown()
                    }
                    latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        formatted = addr.getAddressLine(0) ?: formatted
                        locality = addr.locality ?: addr.subLocality ?: locality
                        district = addr.subAdminArea ?: addr.locality ?: district
                        state = addr.adminArea ?: state
                        pin = addr.postalCode ?: pin
                        street = addr.thoroughfare ?: ""
                        house = addr.featureName ?: ""
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!list.isNullOrEmpty()) {
                        val addr = list[0]
                        formatted = addr.getAddressLine(0) ?: formatted
                        locality = addr.locality ?: addr.subLocality ?: locality
                        district = addr.subAdminArea ?: addr.locality ?: district
                        state = addr.adminArea ?: state
                        pin = addr.postalCode ?: pin
                        street = addr.thoroughfare ?: ""
                        house = addr.featureName ?: ""
                    }
                }
            } catch (_: Exception) {
                // Return reasonable Kashmir default
            }

            // Ensure valid Kashmir district
            val kashmirDistricts = listOf(
                "Srinagar", "Baramulla", "Anantnag", "Budgam", "Pulwama",
                "Kupwara", "Shopian", "Ganderbal", "Kulgam", "Bandipora"
            )
            val matchedDistrict = kashmirDistricts.find { district.contains(it, ignoreCase = true) }
                ?: kashmirDistricts.find { formatted.contains(it, ignoreCase = true) }
                ?: "Srinagar"

            ResolvedAddress(
                latitude = latitude,
                longitude = longitude,
                formattedAddress = formatted,
                houseOrBuilding = house,
                street = street,
                locality = locality,
                district = matchedDistrict,
                state = state,
                pincode = pin
            )
        }

    suspend fun searchKashmirPlaces(query: String): List<ResolvedAddress> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext emptyList()
        val q = query.trim().lowercase()

        val sampleKashmirLocalities = listOf(
            ResolvedAddress(34.0722, 74.8155, "Residency Road, Lal Chowk, Srinagar 190001", "Chinar Complex", "Residency Road", "Lal Chowk", "Near Clock Tower", "Srinagar", "Jammu & Kashmir", "190001"),
            ResolvedAddress(34.0894, 74.8211, "Boulevard Road, Dalgate, Srinagar 190001", "Ghat No. 2", "Boulevard Road", "Dalgate", "Opposite Dal Lake", "Srinagar", "Jammu & Kashmir", "190001"),
            ResolvedAddress(34.1283, 74.8427, "Hazratbal Dargah Road, Hazratbal, Srinagar 190006", "Sir Syed Gate", "University Road", "Hazratbal", "Near Shrine", "Srinagar", "Jammu & Kashmir", "190006"),
            ResolvedAddress(34.0412, 74.7922, "Airport Road, Hyderpora Bypass, Srinagar 190014", "Bypass Plaza", "Airport Road", "Hyderpora", "Near Flyover", "Srinagar", "Jammu & Kashmir", "190014"),
            ResolvedAddress(34.2014, 74.3499, "Fruit Mandi Link Road, Sopore, Baramulla 193201", "Apple Yard 4", "Mandi Road", "Sopore", "Fruit Mandi Gate", "Baramulla", "Jammu & Kashmir", "193201"),
            ResolvedAddress(34.1980, 74.3636, "Main Market, Baramulla Town, Baramulla 193101", "Shop 12", "Carriappa Park Road", "Old Town", "Near Bridge", "Baramulla", "Jammu & Kashmir", "193101"),
            ResolvedAddress(33.7311, 75.1487, "KP Road, Khanabal, Anantnag 192101", "Hospital Link", "KP Road", "Khanabal", "Near Degree College", "Anantnag", "Jammu & Kashmir", "192101"),
            ResolvedAddress(34.0150, 74.7180, "Charar-i-Sharief Road, Budgam Town, Budgam 191111", "DC Office Road", "Main Bazaar", "Budgam", "Near Railway Station", "Budgam", "Jammu & Kashmir", "191111"),
            ResolvedAddress(33.8719, 74.8988, "Court Road, Pulwama Town, Pulwama 192301", "Chowk Complex", "Court Road", "Pulwama", "Near Bus Stand", "Pulwama", "Jammu & Kashmir", "192301"),
            ResolvedAddress(33.7225, 74.8322, "Main Bazaar, Shopian Town, Shopian 192303", "Apple Valley Arcade", "Batapora", "Shopian", "Jamia Masjid Road", "Shopian", "Jammu & Kashmir", "192303"),
            ResolvedAddress(34.3411, 74.2566, "Old Bus Stand, Kupwara Town, Kupwara 193222", "Commercial Hub", "Sopore Road", "Kupwara", "Near Bridge", "Kupwara", "Jammu & Kashmir", "193222"),
            ResolvedAddress(34.2211, 74.7811, "Beehama Chowk, Ganderbal 191201", "Market Complex", "Sindh Road", "Beehama", "Near Sindh River", "Ganderbal", "Jammu & Kashmir", "191201"),
            ResolvedAddress(33.6450, 75.0120, "Main Chowk, Kulgam Town, Kulgam 192231", "Bazaar 2", "Hospital Road", "Kulgam", "Near Shrine", "Kulgam", "Jammu & Kashmir", "192231"),
            ResolvedAddress(34.4210, 74.6420, "Gulshan Chowk, Bandipora 193502", "Wular Complex", "Nishat Road", "Bandipora", "Near Wular View", "Bandipora", "Jammu & Kashmir", "193502"),
            ResolvedAddress(34.0210, 74.9310, "Saffron Highway, Pampore, Pulwama 192121", "Saffron Karewa", "National Highway 44", "Pampore", "Saffron Center", "Pulwama", "Jammu & Kashmir", "192121")
        )

        sampleKashmirLocalities.filter {
            it.formattedAddress.lowercase().contains(q) ||
            it.district.lowercase().contains(q) ||
            it.locality.lowercase().contains(q) ||
            it.street.lowercase().contains(q) ||
            it.landmark.lowercase().contains(q)
        }
    }
}
