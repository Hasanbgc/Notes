package com.example.tempnavigation.helpers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.tempnavigation.receivers.GeoFancingReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.EntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class GeoFancingManger @Inject constructor(
    @ApplicationContext val context: Context,
    val geofencingClient: GeofencingClient
) {
    fun addGeoFancing(id: Int, lat: Double, lon: Double, msg: String = "") {
        val geoFance = Geofence.Builder()
            .setRequestId(id.toString())
            .setCircularRegion(lat, lon, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geoFanceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoFance)
            .build()
        val intent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeoFancingReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(geoFanceRequest, intent)
            .addOnSuccessListener {
                Log.d("GeoFancing", "GeoFancing added")
            }.addOnFailureListener {
                Log.d("GeoFancing", "GeoFancing failed")
            }

    }

    fun removeGeoFancing(id: Int) {
        geofencingClient.removeGeofences(listOf(id.toString()))
            .addOnSuccessListener {
                Log.d("GeoFancing", "GeoFancing removed")
            }.addOnFailureListener {
                Log.d("GeoFancing", "GeoFancing failed")
            }
    }

}