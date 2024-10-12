package com.example.tempnavigation.alarms

interface AlarmSchedulers {
    fun schedule(alarmData:AlarmData)
    fun cancel(alarmData: AlarmData)
}