package com.example.tempnavigation.views.fragments

import android.Manifest
import android.app.Dialog
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationRequestCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.tempnavigation.R
import com.example.tempnavigation.services.MyBroadcastReceiver
import com.example.tempnavigation.services.MyService
import com.example.tempnavigation.utilities.DataCallBack
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.PermissionUtils
import com.example.tempnavigation.views.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import java.util.Calendar


class ProfileFragment : Fragment(),DataCallBack, Dialogs by DialogUtils(){
    private val TAG = "Profile"
    private var myBroadcastReceiver: MyBroadcastReceiver? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationClient:FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var rootView: View
    private lateinit var upButton: ImageView
    private lateinit var textViewNum: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var currentLocation:Location
    private lateinit var bottom_sheet: BottomSheetDialog
    private lateinit var bottomSheetClock: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var textClock:TextClock
    private lateinit var mTimePicker: MaterialTimePicker



    private val locationPermission =  arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)
    private val backgroundLocationPermission =  arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        initView()
        initValue()
       // MyService.startService(requireContext())
        return rootView
    }
    private fun initView(){
        textViewNum = rootView!!.findViewById(R.id.text_view_number)
        startButton = rootView!!.findViewById(R.id.start_button)
        stopButton = rootView!!.findViewById(R.id.stop_button)
        upButton = rootView!!.findViewById(R.id.image_view_up)
        textClock = rootView!!.findViewById(R.id.text_clock)
        val clockDialog = layoutInflater.inflate(R.layout.timer_bottom_sheet,null)
        val dialogs = layoutInflater.inflate(R.layout.bottom_sheet,null)



        bottom_sheet = BottomSheetDialog(requireContext())
        bottom_sheet.setContentView(dialogs)
        val v1 = dialogs.findViewById<LinearLayout>(R.id.location)
        val v4 = dialogs.findViewById<LinearLayout>(R.id.reminder)
        //bottom_sheet.show()

        bottomSheetClock = BottomSheetDialog(requireContext())
        bottomSheetClock.setContentView(clockDialog)
        val timepicker = bottomSheetClock.findViewById<TimePicker>(R.id.timePicker)
        val currentHour = timepicker?.hour
        val currentMin = timepicker?.minute
        //val current = timepicker.
        Log.d(TAG,"currentTime = $currentHour:$currentMin")
        startButton.setOnClickListener {
            Log.d("Profile","button clicked")
            //MyService.startService(requireContext())
            requestLocationPermission()
            getLocation()
        }
        stopButton.setOnClickListener {
            Log.d("Profile","stop button clicked")
            //MyService.stopService(requireContext())
            getLocation()
            showLocation()
        }
        upButton.setOnClickListener{
            bottom_sheet.show()
        }
        v1.setOnClickListener{
            Toast.makeText(requireContext(),"Location",Toast.LENGTH_SHORT).show()
            bottom_sheet.dismiss()
        }
        v4.setOnClickListener{
            bottom_sheet.dismiss()
            materialTimePicker()
            fragmentManager?.let { it1 -> mTimePicker.show(it1,TAG) }
            //bottomSheetClock.show()
        }
        var hr = 0
        var min = 0

        timepicker?.setOnTimeChangedListener(TimePicker.OnTimeChangedListener { view, hourOfDay, minute ->
            hr = hourOfDay
            min = minute
            if(view.hasFocus()){

            }
            Toast.makeText(requireContext(),"current time is $hourOfDay:$minute",Toast.LENGTH_SHORT).show()
        })
        hr = timepicker?.hour!!
        min = timepicker.minute
        Log.d(TAG,"current time is $hr:$min")

    }
    private fun initValue(){
        myBroadcastReceiver = MyBroadcastReceiver(this)
        val filter = IntentFilter("com.example.tempnavigation.data")
        requireActivity().registerReceiver(myBroadcastReceiver,filter)
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        currentLocation = Location("0.0,0.0")
        currentLocation.longitude = 0.0
        currentLocation.latitude = 0.0
        createLocationCallBack()
        createLocationRequest()
    }

    override fun onDataReceive(num: Int?) {
    textViewNum.text = num.toString()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(myBroadcastReceiver)
        myBroadcastReceiver = null
    }
    fun materialTimePicker(){
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
         mTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setPositiveButtonText("Ok")
            .setNegativeButtonText("Cancel")
            .build()
        mTimePicker.addOnPositiveButtonClickListener(View.OnClickListener {
            val am_pm = if(mTimePicker.hour>=12)"PM" else "AM"
            val hr =if (mTimePicker.hour>12) mTimePicker.hour - 12 else mTimePicker.hour
            textClock.setText("${hr}:${mTimePicker.minute} $am_pm")
            mTimePicker.dismiss()
        })
        mTimePicker.addOnNegativeButtonClickListener {
            mTimePicker.dismiss()
        }
    }
    fun CheckLocationPermisson(){
        val permissionUtils = PermissionUtils(requireActivity())
        //permissionUtils.checkLocationPermission(context as FragmentActivity,{},{})
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission->
        permission.forEach(){actionMap->
            when(actionMap.key){
                locationPermission[0] ->{
                    if(actionMap.value){
                        //getfusedLocation
                        doubleButtonDialog(requireActivity(),getString(R.string.bg_location_title),getString(R.string.education_massage_for_bg_location),"Yes","No",{
                            showBackgroundLocationDialog()
                        },{})
                        //Toast.makeText(requireContext(),"location permission = Fine location",Toast.LENGTH_SHORT).show()
                        getLocation()
                    }
                }
                locationPermission[1] ->{
                    if(actionMap.value){
                        //getfusedLocation
                        Toast.makeText(requireContext(),"location permission = Coarse Location",Toast.LENGTH_SHORT).show()

                    }
                }

                backgroundLocationPermission[0]->{
                    if(actionMap.value){
                        Toast.makeText(requireContext(),"location permission = background Location",Toast.LENGTH_SHORT).show()
                        getLocation()
                    }
                }

            }
        }

    }
    fun requestLocationPermission(){
        permissionRequest.launch(locationPermission)
    }
    fun showBackgroundLocationDialog(){
        permissionRequest.launch(backgroundLocationPermission)
    }
    fun createLocationRequest() {
         locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).apply {
             setMinUpdateDistanceMeters(10f)
             setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
             setWaitForAccurateLocation(true)
         }.build()
        Log.d(TAG, "createLocationRequest: ${locationRequest}")
    }
    fun getLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
    }
    fun createLocationCallBack(){
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.let { it ->
                for (location in it.locations){
                    handleLocation(location)
                    Log.d(TAG,"$location")
                }
                }
            }
        }
    }
    fun  handleLocation(location: Location){
      val lat = location.latitude
        val long = location.longitude
        currentLocation.latitude = location.latitude
        currentLocation.longitude = location.longitude
        Toast.makeText(requireContext(),"My current Location is lat:$lat Long:$long",Toast.LENGTH_SHORT).show()
    }
    fun showLocation(){
        Toast.makeText(requireContext(),"My current Location is lat:${currentLocation.latitude} Long:${currentLocation.longitude}",Toast.LENGTH_SHORT).show()
    }
}