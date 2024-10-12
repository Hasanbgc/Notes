package com.example.tempnavigation.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.room.entity.NoteEntity
import com.example.tempnavigation.utilities.LocationManager
import com.example.tempnavigation.viewmodels.LocationCompServiceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationComparatorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var lcsViewModel:LocationCompServiceViewModel
    private lateinit var viewModelStore:ViewModelStore
    private val locationList:ArrayList<Pair<Long,Location>> = ArrayList()

    companion object {
        private const val TAG = "LocationUpdatesService"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = LocationManager(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        viewModelStore = ViewModelStore()
        val factory = object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationCompServiceViewModel(application) as T
            }
        }
        lcsViewModel = ViewModelProvider(viewModelStore,factory)[LocationCompServiceViewModel::class.java]
        //startForeground()
        val intentExtra = IntentFilter("LIST")
        startLocationUpdate()


}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            val list_data = intent.getIntegerArrayListExtra("LIST")
            val listData = intent.getLongArrayExtra("LIST")
            //val context:Context = intent.extras?.get("Context") as Context
            if (listData != null) {
                for(i in listData){
                    lcsViewModel.getNote(listData[i.toInt()],{ it->
                       val noteModel =it.toNoteModel()
                        val location = Location("")
                        if(it.locationLat != 0.0 && it.locationLong!=0.0) {
                            location.latitude = it.locationLat
                            location.longitude = it.locationLong
                            locationList.add(Pair(listData[i.toInt()],location))
                        }
                    },{})
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdate()
        serviceScope.cancel()
    }

    private fun stopLocationUpdate() {
        locationManager.stopLocationUpdates()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
//    private fun startForeground(){
//        val notification = NotificationCompat.Builder(this, "location")
//            .setContentTitle("Tracking Location")
//            .setContentText("Location: ($0.0, $0.0)")
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setOngoing(true)
//
//        notificationManager.notify(NOTIFICATION_ID, notification.build())
//    }
    fun startLocationUpdate(){
        serviceScope.launch {
            try {
                locationManager.setLocationUpdateListener(object : LocationManager.LocationUpdateListener{
                    override fun onLocationUpdated(latitude: Double, longitude: Double) {
                        compareLocationUpdate(latitude,longitude)
                    }

                })
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    private fun compareLocationUpdate(latitude: Double, longitude: Double) {
        val lat = latitude.toString()
        val lng = longitude.toString()
        Log.d(TAG, "Location: ${latitude}, $longitude")

        val currentLocation = Location("")
        currentLocation.longitude = latitude
        currentLocation.longitude = longitude

       for(i in locationList) {
           val id = i.first
           val location = i.second

           val savedLocation = Location("")
           savedLocation.latitude = location.latitude
           savedLocation.longitude = location.longitude

           val distance = savedLocation.distanceTo(currentLocation)

           if (distance isLessThan 20) {
               showAlertDialog(id)
           }
           val notification = NotificationCompat.Builder(this, "location")
               .setContentTitle("Tracking Location")
               .setContentText("Location: ($lat, $lng)")
               .setSmallIcon(R.drawable.ic_launcher_background)
               .setOngoing(true)

           notificationManager.notify(NOTIFICATION_ID, notification.build())
       }
    }

    private fun showAlertDialog(id:Long) {
        val note = getNoteData(id)
        val intent = Intent(applicationContext,AlarmForegroundService::class.java)
        intent.putExtra("TITLE",note.title)
        intent.putExtra("EXTRA_MESSAGE",note.description)
        //applicationContext?.startForegroundService(intent)
    }

    private fun getNoteData(id: Long): NoteModel{
        var noteModel = NoteModel.emptyNote()
        lcsViewModel.getNote(id,{noteModel = it.toNoteModel()},{})
        return noteModel
    }

    //this is the way to create a keyword using extension function
    infix fun Float.isLessThan(Value: Int):Boolean{
        return this < Value
    }

}