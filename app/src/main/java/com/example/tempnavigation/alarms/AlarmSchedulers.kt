package com.example.tempnavigation.alarms

import com.example.tempnavigation.models.AlarmData

interface AlarmSchedulers {
    fun schedule(alarmData: AlarmData)
    fun cancel(alarmData: AlarmData)
}