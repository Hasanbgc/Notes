package com.example.tempnavigation.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.fragment.app.FragmentActivity
import com.example.tempnavigation.R

class PermissionUtils(private val context: Activity) : Permission, Dialogs by DialogUtils() {

    private val locationPermission =  arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    private val backgroundLocationPermission =  arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    private val  cameraPermission = arrayOf(Manifest.permission.CAMERA)
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    private val storagePermission33 = arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO)


     fun permissionForThisApiVersion():Array<String>{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            storagePermission33
        }else{
            storagePermission
        }
    }
    fun locationPermission() = locationPermission
    fun bgLocationPermission() = backgroundLocationPermission
    //region Media/storage permission
    override fun hasMediaAccessPermission(
        context: Activity,
        onPermissonGranted: () -> Unit,
        requestPermission: () -> Unit
    ) {
        when{
            isStorageSelfPermissionGranted(context) ->{
                onPermissonGranted.invoke()
            }
            isStoragePermissionRational(context) ->{
                doubleButtonDialog(context,
                    context.getString(R.string.permission_denied),
                    context.getString(R.string.rational_storage_permission_explanation),
                    "Accept",
                    "No",
                    { requestPermission.invoke() },{})
            }else->{
                requestPermission.invoke()
            }
        }
    }

    private fun isStorageSelfPermissionGranted(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(context, permissionForThisApiVersion()[0]) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(context, permissionForThisApiVersion()[1]) == PackageManager.PERMISSION_GRANTED)
        //tested for storage management permission
//                (ContextCompat.checkSelfPermission(context, permissionForApiVersion()[2]) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isStoragePermissionRational(context: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionForThisApiVersion()[0]) ||
                ActivityCompat.shouldShowRequestPermissionRationale(context, permissionForThisApiVersion()[1])
        //tested for storage management permission
//                ActivityCompat.shouldShowRequestPermissionRationale(context, permissionForApiVersion()[2])
    }
    //endregion

    //region camera permission
    override fun hasCameraAccessPermission(
        context: Activity, onPermissonGranted: () -> Unit, requestPermission: () -> Unit
    ) {
        when{
            checkSelfCameraPermissionGranted(context) -> {
                onPermissonGranted.invoke()
            }
            isCameraPermissionRational(context) -> {
                doubleButtonDialog(context,
                    context.getString(R.string.permission_denied),
                    context.getString(R.string.rational_camera_permission_explanation),
                    "Accept",
                    "No",
                    { requestPermission.invoke() },{})
            }
            else ->{
                requestPermission.invoke()
            }
        }
    }
    private fun checkSelfCameraPermissionGranted(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }
    fun isCameraPermissionRational(context: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(context,cameraPermission[0])
    }
    //endregion

    //region location permission
    override fun hasLocationPermission(context: Activity,onPermissonGranted: () -> Unit,requestPermission: () -> Unit) {
        when{
            checkSelfLocationPermission(context) ->{
                onPermissonGranted.invoke()
            }
            shouldShowRational(context)->{
                doubleButtonDialog(
                    context,
                    context.getString(R.string.permission_denied),
                    context.getString(R.string.rational_location_permission_explanation),
                    "Yes",
                    "No",
                    { requestPermission.invoke() },{})
            }
            else ->{
                requestPermission.invoke()
            }
        }
    }
    fun hasBgLocationPermission(context: Activity,onPermissonGranted: () -> Unit,requestPermission: () -> Unit) {
        when{
            checkSelfBgLocationPermission(context) ->{
                onPermissonGranted.invoke()
            }
            shouldShowRationalforBgLocation(context)->{
                doubleButtonDialog(
                    context,
                    context.getString(R.string.permission_denied),
                    context.getString(R.string.education_massage_for_bg_location),
                    "Yes",
                    "No",
                    { requestPermission.invoke() },{})
            }
            ///TODO(need to handle the don't ask again in permission)
            else ->{
                requestPermission.invoke()
            }
        }
    }

    fun checkSelfLocationPermission(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(context, locationPermission[0]) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(context, locationPermission[1]) == PackageManager.PERMISSION_GRANTED)
    }
    fun shouldShowRational(context: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(context,locationPermission[0])||
                ActivityCompat.shouldShowRequestPermissionRationale(context,locationPermission[1])
    }
    fun checkSelfBgLocationPermission(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(context, bgLocationPermission()[0]) == PackageManager.PERMISSION_GRANTED)}
    fun shouldShowRationalforBgLocation(context: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(context,bgLocationPermission()[0])
    }
    //endregion


    }