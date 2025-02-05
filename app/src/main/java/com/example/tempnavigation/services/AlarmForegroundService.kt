package com.example.tempnavigation.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.impl.foreground.SystemForegroundService
import com.example.tempnavigation.R
import com.example.tempnavigation.alarms.AlarmMediaPlayer
import com.example.tempnavigation.receivers.AlarmDismissReceiver
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.views.MainActivity
import com.example.tempnavigation.views.fragments.AlarmDialogFragment
import com.example.tempnavigation.views.fragments.HomeFragment
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class AlarmForegroundService:Service() {
    private val TAG = "AlarmForegroundService"
    private var alarmTitle:String? = ""
    private var alarmMessage:String? = ""
    private lateinit var contxt:Context
    private lateinit var windowManager:WindowManager
    private lateinit var alertDialogView: View
    private var alarmDialogFragment:AlarmDialogFragment? = null
    private lateinit var noteRepository:NoteRepository
    private lateinit var alarmMediaPlayer: AlarmMediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
        alarmMediaPlayer = AlarmMediaPlayer(this)
        val noteDb = NoteRoomDatabase.getDatabase(applicationContext)
        noteRepository = NoteRepository(noteDb.noteDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service stared!")
        var title = ""
        var msg = ""
        var id = ""
        if (intent?.action == Constant.ALARM_ACTION) {
            title = intent.getStringExtra("TITLE") ?: "Title"
            msg = intent.getStringExtra("EXTRA_MESSAGE") ?: "Message"
        } else {
            id = intent?.getStringExtra("ID")!!
            if (id.isNotEmpty()) {
                noteRepository.getNoteById(id, { noteEntity ->
                    val noteModel = noteEntity.toNoteModel()
                    title = noteModel.title
                    msg = noteModel.description
                }, {})
            }
        }
        val notification = createNotification(title, msg)
        startForeground(1, notification)
        alarmMediaPlayer.prepareMediaPlayer(R.raw.alarm, this.packageName)
        showAlarmDialog(title, msg)
        //stopping service after a timeout
        //Handler(mainLooper).postDelayed({stopSelf()},10000).also { AlarmMediaPlayer(this).stopMediaPlayer() }
        playAlarm()
        return START_NOT_STICKY
    }

    private fun playAlarm() {
        alarmMediaPlayer.startMediaPlayer()
        alarmMediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener{
            override fun onCompletion(mp: MediaPlayer?) {
                alarmMediaPlayer.releaseMediaPlayer()
                Handler(Looper.getMainLooper()).postDelayed({
                    if(alertDialogView.isAttachedToWindow) {
                        windowManager.removeView(alertDialogView)
                    }
                   //] stopSelf() // Uncomment if needed
                }, 5000)
            }

        })
    }

    //this will not work as a system alert Window

    @SuppressLint("ClickableViewAccessibility")
    private fun showAlarmDialog(title:String?, message:String?) {
        Log.d(TAG,"showAlarmDialog called")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        // Initialize your custom view for the alarm dialog
        alertDialogView = LayoutInflater.from(applicationContext).inflate(R.layout.note_alert_dialog_layout, null)

        // Set up your custom dialog view and its components
        val titleTextView = alertDialogView.findViewById<TextView>(R.id.alertTitle)
        val alertMessage = alertDialogView.findViewById<TextView>(R.id.alertMessage)
        val alertOffButton = alertDialogView.findViewById<TextView>(R.id.alert_off_button)
        titleTextView.text = title
        alertMessage.text = message
        Log.d(TAG,"$title,$message")
        alertOffButton.setOnClickListener {
            alarmMediaPlayer.stopMediaPlayer()
            alarmMediaPlayer.releaseMediaPlayer()
            Handler(Looper.getMainLooper()).apply {
                if (alertDialogView.isAttachedToWindow) {
                    windowManager.removeView(alertDialogView)
                }
            }
            stopSelf()
        }
        // ...

        // Configure the WindowManager.LayoutParams for the system alert window
        val layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )
        }
        layoutParams.gravity = Gravity.TOP

        var initialX = 0f
        alertDialogView.setOnTouchListener{_,event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    val viewWidth = alertDialogView?.width ?: 0

                    if (deltaX >= viewWidth * 0.30) {
                        // Dismiss the dialog
                        windowManager.removeView(alertDialogView)
                        stopSelf()
                    } else {
                        val translationX = deltaX.coerceAtMost(viewWidth * 0.30f)
                        alertDialogView.translationX = translationX
                    }
                    true
                }
                MotionEvent.ACTION_UP ->{
                    alertDialogView.animate()?.translationX(0f)?.start()
                    true
                }else -> {false}
            }
        }

        // Add the custom view to the WindowManager as a system alert window
        windowManager.addView(alertDialogView, layoutParams)


    }
    private fun createNotification(title:String?,message:String?): Notification {
        Log.d(TAG,"Notification has crated")
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, Constant.CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(
                PendingIntent.getActivity(this,0,
                    Intent(this, HomeFragment::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(NotificationCompat.Action(0,
                "Dismiss",
                PendingIntent.getBroadcast(this,0,Intent(this,AlarmDismissReceiver::class.java),PendingIntent.FLAG_IMMUTABLE)))
            .build()
        notificationManager.notify(1, notification)
        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmMediaPlayer.apply {
            startMediaPlayer()
            releaseMediaPlayer()
        }
        //windowManager.removeView(alertDialogView)
    }

}
fun Context.startServiceWithIntent(intent: Intent){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)  // For API 26+
    } else {
        this.startService(intent)  // For API 25 and below
    }
}