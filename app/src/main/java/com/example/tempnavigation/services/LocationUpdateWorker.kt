package com.example.tempnavigation.services

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tempnavigation.receivers.AlarmReceiver
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.utilities.LocationManager

class LocationUpdateWorker(val context:Context, workerParameters: WorkerParameters):CoroutineWorker(context,workerParameters) {
    private val TAG = "LocationUpdateWorker"
    private val locationManager by lazy { LocationManager(context) }
    private val notificationManager by lazy { ContextCompat.getSystemService(context,NotificationManager::class.java) as NotificationManager }
    private lateinit var  locationList: List<Pair<Int,Location>>

    override suspend fun doWork(): Result {
        return try {
           // val list = inputData.keyValueMap<>
            locationList = inputData.getStringArray(Constant.KEY_LOCATION_LIST)?.map { locationString ->
                val parts = locationString.split(",")
                val latitude = parts[1].toDouble()
                val longitude = parts[2].toDouble()
                val location = Location("").apply {
                    setLatitude(latitude)
                    setLongitude(longitude) }
                Pair(parts[0].toInt(), location) } ?: emptyList()
                //Pair(parts[0].toInt(), Location(parts[1].toDouble(), parts[2].toDouble())) } ?: emptyList()
            Log.d(TAG,"$locationList")

           // sendLocationUpdateBroadcast(1)
            locationManager.setLocationUpdateListener(object : LocationManager.LocationUpdateListener{
                override fun onLocationUpdated(latitude: Double, longitude: Double) {
                    Log.d(TAG, "On LocationUpdated: $latitude, $longitude")
                    compareLocationUpdate(latitude,longitude)
                }
            })
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    private fun compareLocationUpdate(latitude: Double, longitude:Double){

        Log.d(TAG, "Location: ${latitude}, $longitude")

        val currentLocation = Location("")
        currentLocation.latitude = latitude
        currentLocation.longitude = longitude
        for(item in locationList) {
            var id = 0
            var location = Location("")
            if(item is Pair<*, *>) {
                id = item.first as Int
                location = item.second as Location
            }
            Log.d(TAG, "compareLocationUpdate: id = ${id}, location = ${location}")

            val savedLocation = Location("")
            savedLocation.latitude = location.latitude
            savedLocation.longitude = location.longitude

            val distance = savedLocation.distanceTo(currentLocation)

            if (distance isLessThan 3) {
              sendLocationUpdateBroadcast(id)
            }
//            val notification = NotificationCompat.Builder(this, "location")
//                .setContentTitle("Tracking Location")
//                .setContentText("Location: ($lat, $lng)")
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .setOngoing(true)
//
//            notificationManager.notify(LocationComparatorService.NOTIFICATION_ID, notification.build())
        }

    }

    private fun sendLocationUpdateBroadcast(id: Int) {

        val intent = Intent(context,AlarmReceiver::class.java)
        intent.action = "LOCATION_UPDATE_ACTION"
        intent.putExtra("ID",id)
        context.sendBroadcast(intent).also {
            Log.d(TAG, "i sent my broadcast")
        }
    }

    private infix fun Float.isLessThan(Value: Int):Boolean{
        return this < Value
    }
}