package com.example.tempnavigation.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.example.tempnavigation.R
import com.example.tempnavigation.utilities.LocationManager
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton

class MapsFragment : Fragment(),LocationManager.LocationUpdateListener {
    val TAG = "MapFragment"
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var rootView: View
    private lateinit var setButton: MaterialButton
    private lateinit var locationManager: LocationManager
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var clickedLatitude: Double = 0.0
    private var clickedLongitude: Double = 0.0
    private lateinit var googleMap: GoogleMap
    private var mapFragment = SupportMapFragment()

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        updateMapWithCurrentLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView =  inflater.inflate(R.layout.fragment_maps, container, false)
        setHasOptionsMenu(true)
        initView()
        initValues()
        locationManager = LocationManager(requireContext())
        locationManager.setLocationUpdateListener(this)

        return rootView
    }
    private fun initView() {
        mapFragment =
            (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)!!
        mapFragment?.getMapAsync(callback)
        setButton = rootView.findViewById(R.id.set_location_button)
        setButton.setOnClickListener{
            mainViewModel.navigationPage.value = NavigationPage.NAVIGATE_UP
            updateLocation()
        }
    }

    private fun updateLocation() {
        mainViewModel.selectedLocation.value = Pair(clickedLatitude,clickedLongitude)
    }

    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
        locationManager.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationManager.stopLocationUpdates()
    }
    private fun initValues() {
        mainViewModel.showBottomNav.value = false
        mainViewModel.title.value = "Map"

        //locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mainViewModel.navigationPage.value = NavigationPage.NAVIGATE_UP
                updateLocation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateMapWithCurrentLocation() {
        val currentLatLng = LatLng(currentLatitude, currentLongitude)
        googleMap.clear()
        val markerOptions = MarkerOptions().position(currentLatLng).title("Marker")
        googleMap.addMarker(markerOptions)

        googleMap.setOnMapClickListener { clickedLatLng ->
            // Update the marker position when the map is clicked
            googleMap.clear()
            markerOptions.position(clickedLatLng)
            googleMap.addMarker(markerOptions)

            // Retrieve the latitude and longitude of the clicked location
            clickedLatitude = clickedLatLng.latitude
            clickedLongitude = clickedLatLng.longitude
            Log.d(TAG, "Clicked Location = Lat: $clickedLatitude, Long: $clickedLongitude")
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 25f))

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
        googleMap.setOnMyLocationButtonClickListener {
            googleMap.clear()
            markerOptions.position(currentLatLng)
            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 30f))
            return@setOnMyLocationButtonClickListener true
        }
    }
    override fun onLocationUpdated(latitude: Double, longitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
        updateMapWithCurrentLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
    }
//    fun  handleLocation(location: Location){
//        currentLatitude = location.latitude
//        currentLongitude = location.longitude
//        updateMapWithCurrentLocation()
//        Log.d(TAG,"current Location = $location")
//        Toast.makeText(requireContext(),"My current Location is lat:$currentLatitude Long:$currentLongitude", Toast.LENGTH_SHORT).show()
//    }
//    fun showLocation(){
//        Toast.makeText(requireContext(),"My current Location is lat:${currentLatitude} Long:${currentLongitude}",
//            Toast.LENGTH_SHORT).show()
//    }
    //    fun createLocationRequest() {
//        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000).apply {
//            setMinUpdateDistanceMeters(10f)
//            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
//            setWaitForAccurateLocation(true)
//        }.build()
//        Log.d(TAG, "createLocationRequest: ${locationRequest}")
//    }
//    fun getLocation(){
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            locationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.getMainLooper())
//        }
//    }
//    fun createLocationCallBack(){
//        locationCallback = object : LocationCallback(){
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.let { it ->
//                    for (location in it.locations){
//                        handleLocation(location)
//                        Log.d(TAG,"$location")
//                    }
//                }
//            }
//        }
//    }


}