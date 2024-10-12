package com.example.tempnavigation.alarms

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.tempnavigation.R
import com.example.tempnavigation.services.AlarmForegroundService
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.views.fragments.AlarmDialogFragment

class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG,"it got msg to start")
        if(intent?.action == "ALARM_SCHEDULER_ACTION" ){
            val title = intent.getStringExtra("TITLE")
            val msg = intent.getStringExtra("EXTRA_MESSAGE")
            val serviceIntent = Intent(context,AlarmForegroundService::class.java)
            serviceIntent.action = "ALARM_SCHEDULER_ACTION"
            serviceIntent.putExtra("TITLE",title)
            serviceIntent.putExtra("EXTRA_MESSAGE",msg)
            context?.startForegroundService(serviceIntent).also { Log.d(TAG,"Alarm service has been started from ALARM_SCHEDULER_ACTION ") }
        }else if( intent?.action == "LOCATION_UPDATE_ACTION" ){
            val id = intent.getIntExtra("ID",0)
            val serviceIntent = Intent(context,AlarmForegroundService::class.java)
            serviceIntent.action = "LOCATION_UPDATE_ACTION"
            serviceIntent.putExtra("ID",id)
            context?.startForegroundService(serviceIntent).also { Log.d(TAG,"Alarm service has been started from LOCATION_UPDATE_ACTION ") }
        }

//        val title = intent?.getStringExtra("TITLE")
//        val message = intent?.getStringExtra("EXTRA_MESSAGE")
//        context?.let { ctx ->
//            val alarmMediaPlayer = AlarmMediaPlayer(ctx)
//            alarmMediaPlayer.prepareMediaPlayer(R.raw.alarm, ctx.packageName)
//
//            alarmMediaPlayer.startMediaPlayer()
//
////            val notificationManager =
////                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////            val notification = NotificationCompat.Builder(ctx, Constant.CHANNEL_ID)
////                .setSmallIcon(R.drawable.ic_launcher_foreground)
////                .setContentTitle("Alarm Demo")
////                .setContentText("Notification sent with message $message")
////                .setPriority(NotificationCompat.PRIORITY_HIGH)
////                .build()
////            notificationManager.notify(1,notification)
//
//        }
//        val intent = Intent(context,AlarmForegroundService::class.java)
//        intent.putExtra("TITLE",title)
//        intent.putExtra("EXTRA_MESSAGE",message)
//        context?.startForegroundService(intent)

    }
}