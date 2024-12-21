package com.example.tempnavigation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tempnavigation.services.AlarmForegroundService

class AlarmDismissReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context,AlarmForegroundService::class.java)
        context?.stopService(serviceIntent)
    }

}