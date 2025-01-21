package com.example.tempnavigation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tempnavigation.services.AlarmForegroundService
import com.example.tempnavigation.services.startServiceWithIntent

class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context,AlarmForegroundService::class.java)
        Log.d(TAG,"it got msg to start")
        if(intent?.action == "ALARM_SCHEDULER_ACTION" ){
            val title = intent.getStringExtra("TITLE")
            val msg = intent.getStringExtra("EXTRA_MESSAGE")
            serviceIntent.action = "ALARM_SCHEDULER_ACTION"
            serviceIntent.putExtra("TITLE",title)
            serviceIntent.putExtra("EXTRA_MESSAGE",msg)
            Log.d(TAG,"Alarm service has been started from ALARM_SCHEDULER_ACTION ")
        }else if( intent?.action == "LOCATION_UPDATE_ACTION" ){
            val id = intent.getStringExtra("ID")
            val serviceIntent = Intent(context,AlarmForegroundService::class.java)
            serviceIntent.action = "LOCATION_UPDATE_ACTION"
            serviceIntent.putExtra("ID",id)
            Log.d(TAG,"Alarm service has been started from LOCATION_UPDATE_ACTION ")
        }
        context?.startServiceWithIntent(serviceIntent)
    }
}