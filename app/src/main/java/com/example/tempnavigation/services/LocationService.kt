package com.example.tempnavigation.services

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.tempnavigation.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createLocationRequest()
        createLocationCallback()
        startForegroundService()
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createLocationRequest() {
         locationRequest = LocationRequest.Builder(10000)
             .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
             .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for (location in it.locations) {
                        handleLocationUpdate(location)
                    }
                }
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        val lat = location.latitude.toString()
        val lng = location.longitude.toString()
        Log.d(TAG, "Location: ($lat, $lng)")

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking Location")
            .setContentText("Location: ($lat, $lng)")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking Location")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun startLocationUpdates() {
        serviceScope.launch {
            try {
                val locationFlow = getLocationUpdates()
                locationFlow.collect { location ->
                    handleLocationUpdate(location)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermissions()) {
            throw LocationUpdatesException("Missing Permissions for Location")
        }

        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled) {
            throw LocationUpdatesException("GPS is disabled")
        }


        if(ContextCompat.checkSelfPermission(
                this@LocationService,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this@LocationService,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
            }



        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "LocationUpdatesService"
        private const val INTERVAL: Long = 5000 // 5 seconds
        private const val FASTEST_INTERVAL: Long = 2000 // 2 seconds
        private const val NOTIFICATION_ID = 1
    }

    inner class LocationUpdatesException(message: String) : Exception(message)
}