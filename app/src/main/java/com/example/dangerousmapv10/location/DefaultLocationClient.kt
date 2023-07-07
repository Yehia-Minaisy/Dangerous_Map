package com.example.dangerousmapv10.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.example.dangerousmapv10.hasLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationClient {
    @SuppressLint("MissingPermission")

    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if (!context.hasLocationPermission()) {

               throw LocationClient.LocationException("missing Location permission")
            }
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")

            }


            val request = LocationRequest.Builder(interval)
                .setPriority(100)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location)
                        }
                    }
                }
            }
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            client.lastLocation.addOnSuccessListener { locatin:Location?->
                if (locatin!=null){

                }
            }


            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}