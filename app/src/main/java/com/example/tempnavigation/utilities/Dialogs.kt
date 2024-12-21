package com.example.tempnavigation.utilities

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.view.View
import com.example.tempnavigation.services.MyService

interface Dialogs {
    fun doubleButtonDialog(
        context:Activity,
        title: String,
        message: String,
        yesButton: String,
        noButton: String,
        onYesClick: () -> Unit,
        onNoClick: () -> Unit
    )

    fun singleButtonDialog(
        context:Context,
        title: String,
        message: String,
        yesButton: String,
        noButton: String,
        onYesClick: () -> Unit
    )

    fun progressDialog(context: Context)

    fun showSnackBarWithActionButton(
        context:Context,
        view: View,
        msg:Int,
        confirmationMsg:Int,
        onUndo: ()->Unit,
        onTimeout: ()->Unit)


}