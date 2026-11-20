package com.example.campuscare.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun isDeviceLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun verifyLocationSettings(
        context: Context,
        onSettingsSatisfied: () -> Unit,
        onResolutionNeeded: (ResolvableApiException) -> Unit,
        onFailed: (Exception) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onSettingsSatisfied()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                onResolutionNeeded(exception)
            } else {
                onFailed(exception)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): String? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null

        val location = getFreshLocation(context) ?: getLastKnownLocation(context)
        if (location != null) {
            formatAddress(context, location)
        } else {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFreshLocation(context: Context): Location? {
        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocation(context: Context): Location? {
        // Try fused client lastLocation
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val loc: Location? = suspendCancellableCoroutine { cont ->
                fusedClient.lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
            if (loc != null) return loc
        } catch (_: Exception) {
            // fallback
        }

        // Fallback to system LocationManager
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            gpsLoc ?: netLoc
        } catch (_: Exception) {
            null
        }
    }

    private fun formatAddress(context: Context, location: Location): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    formatAddressObject(address, location)
                } else {
                    formatCoordinates(location)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    formatAddressObject(address, location)
                } else {
                    formatCoordinates(location)
                }
            }
        } catch (_: Exception) {
            formatCoordinates(location)
        }
    }

    private fun formatAddressObject(address: android.location.Address, location: Location): String {
        val parts = mutableListOf<String>()

        val subLocality = address.subLocality ?: address.thoroughfare
        if (!subLocality.isNullOrBlank()) {
            parts.add(subLocality)
        }

        val locality = address.locality ?: address.subAdminArea
        if (!locality.isNullOrBlank() && locality != subLocality) {
            parts.add(locality)
        }

        return if (parts.isNotEmpty()) {
            "${parts.joinToString(", ")} (Lat: ${String.format(Locale.US, "%.4f", location.latitude)}, Lng: ${String.format(Locale.US, "%.4f", location.longitude)})"
        } else {
            address.getAddressLine(0) ?: formatCoordinates(location)
        }
    }

    private fun formatCoordinates(location: Location): String {
        return "Lat: ${String.format(Locale.US, "%.4f", location.latitude)}, Lng: ${String.format(Locale.US, "%.4f", location.longitude)}"
    }
}
