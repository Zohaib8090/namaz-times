package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String
)

object LocationHelper {

    val KARACHI_DEFAULT = AppLocation(
        latitude = 24.8607,
        longitude = 67.0011,
        name = "Karachi, Pakistan"
    )

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): AppLocation = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext KARACHI_DEFAULT
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            val location: Location? = withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine { continuation ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (continuation.isActive) {
                                if (loc != null) {
                                    continuation.resume(loc)
                                } else {
                                    // Try last location
                                    fusedClient.lastLocation
                                        .addOnSuccessListener { lastLoc ->
                                            if (continuation.isActive) continuation.resume(lastLoc)
                                        }
                                        .addOnFailureListener {
                                            if (continuation.isActive) continuation.resume(null)
                                        }
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }

                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }

            if (location != null) {
                val cityName = resolveLocationName(context, location.latitude, location.longitude)
                AppLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    name = cityName
                )
            } else {
                KARACHI_DEFAULT
            }
        } catch (e: Exception) {
            KARACHI_DEFAULT
        }
    }

    private suspend fun resolveLocationName(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val resolved = withTimeoutOrNull(3000L) {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Karachi"
                            val country = addr.countryName ?: "Pakistan"
                            "$city, $country"
                        } else null
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Karachi"
                            val country = addr.countryName ?: "Pakistan"
                            "$city, $country"
                        } else null
                    }
                } else null
            }
            if (resolved != null) {
                return@withContext resolved
            }

            // Fallback based on coordinates proximity
            val distToKarachi = Math.hypot(lat - 24.8607, lng - 67.0011)
            if (distToKarachi < 0.5) {
                "Karachi, Pakistan"
            } else {
                String.format(Locale.ENGLISH, "%.3f°, %.3f°", lat, lng)
            }
        } catch (e: Exception) {
            "Karachi, Pakistan"
        }
    }
}
