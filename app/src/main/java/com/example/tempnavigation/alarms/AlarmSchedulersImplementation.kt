package com.example.tempnavigation.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmSchedulersImplementation(val context: Context) : AlarmSchedulers {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarmData: AlarmData) {
        val intent = Intent(context,AlarmReceiver::class.java).apply {
            action = "ALARM_SCHEDULER_ACTION"
            putExtra("TITLE",alarmData.title)
            putExtra("EXTRA_MESSAGE", alarmData.message)
        }
        val pendingIntent = PendingIntent.getBroadcast(context,alarmData.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            convertToLocalDateTime(alarmData.alarmTime).atZone(ZoneId.systemDefault()).toEpochSecond()*1000L
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: Date
            try {
                date = sdf.parse(alarmData.alarmTime) as Date
                 date.time
            } catch (e: ParseException) {
                e.printStackTrace()
                0L // Placeholder value
            }
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,alarmTime,pendingIntent)
        Log.e("Alarm", "Alarm set at $alarmTime")

    }


    override fun cancel(alarmData: AlarmData) {
        val intent = Intent(context,AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context,alarmData.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToLocalDateTime(dateTimeString: String): LocalDateTime {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateTimeString)
        val calendar = Calendar.getInstance().apply { time = date }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        return LocalDateTime.of(year, month, day, hour, minute, second).also { Log.d("AlarmSchedulersImplementation",LocalDateTime.of(year, month, day, hour, minute, second).toString()) }
    }
}