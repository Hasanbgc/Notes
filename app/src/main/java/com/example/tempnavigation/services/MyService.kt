package com.example.tempnavigation.services

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.tempnavigation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MyService: Service() {

    private lateinit var notificationManager: NotificationManager
    var runUntil = true
    var sticky = true
    companion object{
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "MyServiceChannel"
        const val RANDOM_NUMBER_ACTION_ID = "com.example.tempnavigation.data"
        const val INTERVAL = 1000L
        fun startService(context: Context) {
            val startIntent = Intent(context, MyService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {

            Log.d("Myservice","StopService called")
            val stopIntent = Intent(context, MyService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initValue()
        //createForegroundService()
        generateRandomNumber()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("MyService","called from onStartService")
        createForegroundService()
        return START_STICKY
    }
    private fun initValue(){
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private fun generateRandomNumber(){
        val coroutine = CoroutineScope(Dispatchers.Main)
        coroutine.launch {
            while (runUntil) {
                delay(INTERVAL)
                val num = Random.nextInt(100)
                sendData(num)
                Log.d("MyService", "Random Number is: $num")
                updateNotification(num)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createForegroundService(){
        val notificationChannel = NotificationChannel(CHANNEL_ID,"ServiceForRandomNumber",NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RandomNumberUpdate")
            .setContentText("Random Number: ")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(sticky)
            .build()

        startForeground(NOTIFICATION_ID,notification)
    }
    private fun updateNotification(num:Int){
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RandomNumberUpdate")
            .setContentText("Random Number: $num")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(sticky)
            .build()
        notificationManager.notify(NOTIFICATION_ID,notification)
    }
    private fun sendData(num:Int){
        val intent = Intent()
        intent.action = RANDOM_NUMBER_ACTION_ID
        intent.putExtra("RandomNumber",num)
        sendBroadcast(intent)

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Myservice","onDestroy called")
        runUntil = false
        sticky = false
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }
}