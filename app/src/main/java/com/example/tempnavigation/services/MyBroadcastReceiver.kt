package com.example.tempnavigation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tempnavigation.utilities.DataCallBack

open class MyBroadcastReceiver(private val callBack: DataCallBack): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getIntExtra("RandomNumber",0)
        Log.d("Broadcast","$data")
        callBack.onDataReceive(data)
    }
}