package com.example.tempnavigation.utilities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tempnavigation.R

class PermissionUtils : Permission, Dialogs by DialogUtils() {

    //region storage permission
    override fun checkMediaPermission(
        context: Activity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

    //endregion


    //region camera permission
    override fun checkCameraAccessPermission(
        context: Activity, onSuccess: () -> Unit, onFailed: () -> Unit
    ) {
        if (isCheckSelfCameraPermissionNotGranted(context)) {
            showCameraPermissionDialog(context)
        }else {

        }

    }
    private fun isCheckSelfCameraPermissionNotGranted(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_DENIED)
    }
    private fun showCameraPermissionDialog(context: Activity) {
        ActivityCompat.requestPermissions(
            context, arrayOf(
                android.Manifest.permission.CAMERA
            ), Constant.REQUEST_ACCESS_CAMERA
        )
    }
    //endregion

    }