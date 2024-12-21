package com.example.tempnavigation.views.fragments

import android.Manifest
import android.app.Dialog
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Card
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationRequestCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.example.tempnavigation.R
import com.example.tempnavigation.services.MyBroadcastReceiver
import com.example.tempnavigation.services.MyService
import com.example.tempnavigation.utilities.DataCallBack
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.PermissionUtils
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.MainViewModel
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
import kotlin.getValue


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

    private val mainViewModel: MainViewModel by activityViewModels()



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
        /*startButton.setOnClickListener {
            Log.d("Profile","button clicked")
            //MyService.startService(requireContext())
            requestLocationPermission()
            getLocation()
        }*/
        var update = 0
        startButton.setOnLongClickListenerForRepeatedUIUpdate(100L, onLongClick = {
            update++
            textViewNum.text = update.toString()
        }, onCancel = {})
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
            textClock.text = "${hr}:${mTimePicker.minute} $am_pm"
            mTimePicker.dismiss()
        })
        mTimePicker.addOnNegativeButtonClickListener {
            mTimePicker.dismiss()
        }
    }
    fun CheckLocationPermisson(){
        PermissionUtils(requireActivity())
        //permissionUtils.checkLocationPermission(context as FragmentActivity,{},{})
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permission->
        permission.forEach { actionMap->
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

    fun View.setOnLongClickListenerForRepeatedUIUpdate(
        duration: Long = 1000L,
        onLongClick: () -> Unit,
        onCancel: () -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        var isLongPressing = false
        val longClickRunnable = object : Runnable {
            override fun run() {
                if (isLongPressing) {
                    onLongClick()
                    handler.postDelayed(this, duration) // Repeatedly call every `duration`
                }
            }
        }

        setOnLongClickListener {
            isLongPressing = true
            handler.postDelayed(longClickRunnable, 400) // Initial delay
            true // Indicate that the long click was handled
        }

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isLongPressing) {
                        isLongPressing = false
                        handler.removeCallbacks(longClickRunnable) // Stop the updates
                        onCancel() // Trigger the cancel callback
                    }
                }
            }
            false // Allow normal click/long-click behavior
        }
    }

   /* @Composable
    fun FabCradleBottomNavigationBar(
        selectedTab: String,
        onTabSelected: (String) -> Unit,
        onFabClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bottom Navigation Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Navigation Items
                    //val Icons = null
                    BottomNavItem(
                        label = "Favourite",
                        icon = ImageVector.vectorResource(R.drawable.favorite_border_24).apply { tintColor},

                        isSelected = selectedTab == "favourite",
                        onClick = { onTabSelected("home") }
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Space for FAB Cradle

                    // Right Navigation Items
                    BottomNavItem(
                        label = "Home",
                        icon = ImageVector.vectorResource(R.drawable.home_24),
                        isSelected = selectedTab == "home",
                        onClick = { onTabSelected("profile") }
                    )
                }
            }

            // FAB Positioned Above the Navigation Bar
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-4).dp), // Adjust to "cradle" the FAB into the bar
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(ImageVector.vectorResource(R.drawable.add_24) , contentDescription = "Add")
            }
        }
    }

    @Composable
    fun BottomNavItem(
        label: String,
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colors.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colors.primary else Color.Gray,
                style = MaterialTheme.typography.caption
            )
        }
    }


    @Composable
    fun CradleCutoutBottomBarDemo(
        selectedTab: String,
        onTabSelected: (String) -> Unit,
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { *//* Handle FAB click *//* }
                ) {
                    Icon(ImageVector.vectorResource(R.drawable.add_24), contentDescription = "Add")
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true, // Ensure the FAB docks into the BottomAppBar
            bottomBar = {
                BottomAppBar(
                    cutoutShape = CircleShape, // Cradle cutout for a circular FAB
                    backgroundColor = colorResource(R.color.bottomNavBar),
                ) {
                    // Bottom navigation items
                    BottomNavItem(
                        label = "Favourite",
                        icon = ImageVector.vectorResource(R.drawable.favorite_border_24).apply { tintColor},

                        isSelected = selectedTab == "favourite",
                        onClick = { onTabSelected("home") }
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Space for FAB Cradle

                    // Right Navigation Items
                    BottomNavItem(
                        label = "Home",
                        icon = ImageVector.vectorResource(R.drawable.home_24),
                        isSelected = selectedTab == "home",
                        onClick = { onTabSelected("profile") }
                    )
                }
            }
        ) {
            // Main screen content
            Text("Content goes here", Modifier.padding(it))
        }
    }


    @Composable
    @Preview
    fun BottomBarPreview(){
        *//*   FabCradleBottomNavigationBar("Home",
               onTabSelected = {it->
                   Toast.makeText(context,"$it", Toast.LENGTH_SHORT).show()
                   when(it){
                     "home"-> mainViewModel.navigationPage.value = NavigationPage.HOME
                     "archive"-> mainViewModel.navigationPage.value = NavigationPage.ARCHIVE
                       "favourite"->mainViewModel.navigationPage.value = NavigationPage.FAVOURITE
               }
               },
               onFabClick = {
                   mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE
               }
               )*//*
        CradleCutoutBottomBarDemo(
            "Home",
            onTabSelected ={it->
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                when(it){
                    "home"-> mainViewModel.navigationPage.value = NavigationPage.HOME
                    "archive"-> mainViewModel.navigationPage.value = NavigationPage.ARCHIVE
                    "favourite"->mainViewModel.navigationPage.value = NavigationPage.FAVOURITE
                }
            }
        )
    }*/
}