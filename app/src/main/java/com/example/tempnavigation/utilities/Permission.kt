package com.example.tempnavigation.utilities

import android.app.Activity
import androidx.fragment.app.FragmentActivity

interface Permission {

    fun hasMediaAccessPermission(
        context: Activity,
        onPermissionGranted: () -> Unit,
        requestPermission: () -> Unit
    )

    fun hasCameraAccessPermission(
        context: Activity,
        onPermissionGranted: () -> Unit,
        requestPermission: () -> Unit
    )
    ///TODO

    /*fun checkInternetPermission()*/
    fun hasLocationPermission(
        fragmentActivity: Activity,
        onPermissionGranted: () -> Unit,
        requestPermission: () -> Unit
    )
}