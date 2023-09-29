package com.example.tempnavigation.utilities

import android.app.Activity

interface Permission {

    fun checkMediaPermission(
        context: Activity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    )

    fun checkCameraAccessPermission(
        context: Activity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    )
    ///TODO
    /*
    fun checkInternetPermission()
    fun checkLocationPermission()*/
}