package com.example.tempnavigation.services

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tempnavigation.utilities.LocationManager
import com.example.tempnavigation.viewmodels.LocationCompServiceViewModel
import kotlinx.coroutines.launch

class LocationUpdateWorker(val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    private val TAG = "LocationUpdateWorker"

    private val viewModel: LocationCompServiceViewModel by lazy {
        val appContext = context.applicationContext as Application
        LocationCompServiceViewModel(appContext)
    }
    private val locationManager by lazy { LocationManager(context) }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: has been started")
        return try {
            locationManager.startLocationUpdates()
            locationManager.setLocationUpdateListener(object :
                LocationManager.LocationUpdateListener {
                override fun onLocationUpdated(latitude: Double, longitude: Double) {
                    //Log.d(TAG, "On LocationUpdated: $latitude, $longitude")
                    viewModel.viewModelScope.launch {
                        Log.d(TAG, "launch: has been started")
                        val list = viewModel.getNoteNearbyLocation(latitude, longitude, 20.0)
                        compareLocationUpdate(latitude, longitude, list)
                    }

                }
            })
            Result.retry()
        } catch (e: Exception) {
            Log.d(TAG, "error is : ${e.message}")
            Result.failure()
        }
    }

    private fun compareLocationUpdate(latitude: Double, longitude: Double, list: List<Pair<String, Pair<Double, Double>>>) {

        val currentLocation = Location("")
        currentLocation.latitude = latitude
        currentLocation.longitude = longitude

        Log.d("LocationUpdateWorker", "getNoteNearbyLocation: $list")
        for (item in list) {

            val savedLocation = Location("")
            savedLocation.latitude = item.second.first
            savedLocation.longitude = item.second.second

            val distance = savedLocation.distanceTo(currentLocation)
            Log.d("LocationUpdateWorker", "distance: $distance")
            if (distance isLessThan 15) {
                startAlarmService(item.first)
            }
        }

    }

    fun startAlarmService(id: String) {
        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        serviceIntent.action = "LOCATION_UPDATE_ACTION"
        serviceIntent.putExtra("ID", id)
        context.startService(serviceIntent)
    }

    private infix fun Float.isLessThan(Value: Int): Boolean {
        return this < Value
    }
}