package com.example.tempnavigation.utilities

import android.app.Activity
import android.content.Context

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
}