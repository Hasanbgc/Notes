package com.example.tempnavigation.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tempnavigation.R
import com.example.tempnavigation.alarms.AlarmMediaPlayer
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.services.LocationComparatorService
import com.example.tempnavigation.services.LocationUpdateWorker
import com.example.tempnavigation.services.MyService
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener, Dialogs by DialogUtils() {
    val TAG = "MainActivity"
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerNavView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var toolBar: Toolbar

    private lateinit var titleView: TextView
    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView
    private lateinit var addFab: FloatingActionButton
    private lateinit var bottomAppBar: BottomAppBar

    private lateinit var alarmMediaPlayer: AlarmMediaPlayer

    private val workManager = WorkManager.getInstance(this)
    //var list = arrayListOf<Pair<Int,Location>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavigation()
        initWindow()
        initView()
        initValue()
        observeLiveData()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constant.CHANNEL_ID,
                Constant.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply { setSound(null, audioAttributes) }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(notificationChannel)
        } else {
            // For API 24 and lower, create a notification channel using NotificationCompat
            val builder = NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                .setContentTitle(Constant.CHANNEL_NAME)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(getSoundUri())

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, builder.build())
        }
    }

    private fun getSoundUri(): Uri {
        val soundFile = "alarm.mp3"
        return Uri.parse("android.resource://${packageName}/raw/${soundFile}")
    }

    private fun initWindow() {
        toolBar = findViewById(R.id.toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.title = ""
        //supportActionBar?.setHomeButtonEnabled(true)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initNavigation() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerNavView = findViewById(R.id.drawer_nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                R.id.navigation_favourite -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                R.id.navigation_archive -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                else -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        drawerNavView.setupWithNavController(navController)

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_favourite,
                R.id.navigation_archive
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun initView() {
        fragmentContainerView = findViewById(R.id.nav_host_fragment)
        titleView = findViewById(R.id.text_view_title)
        bottomNavView = findViewById(R.id.bottom_nav_view)
        addFab = findViewById(R.id.addButton)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        addFab.setOnClickListener(this)
        viewModel.insertInitialHomeViewStyle()
    }

    private fun initValue() {
        alarmMediaPlayer = AlarmMediaPlayer(this)
        alarmMediaPlayer.prepareMediaPlayer(0, packageName)
    }

    override fun onSupportNavigateUp(): Boolean {
        //textViewUserName.text = viewModel.getUserName()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {

            navController.currentDestination?.let {
                if (it.id == R.id.navigation_home) {
                    doubleButtonDialog(this,
                        "Close The App",
                        "Do You Want To Close The App?",
                        "YES",
                        "NO",
                        { finish() }, {})
                } else {
                    Toast.makeText(this, "backpressed clicked", Toast.LENGTH_SHORT).show()
                    super.onBackPressed()
                }
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addButton -> {
                viewModel.navigationPage.value = NavigationPage.ADD_NOTE
            }
        }
    }

    private fun observeLiveData() {
        viewModel.navigationPage.observe(this, Observer {
            when (it) {
                NavigationPage.NAVIGATE_UP -> navController.navigateUp()
                NavigationPage.HOME -> navController.navigate(R.id.navigation_home)
                NavigationPage.FAVOURITE -> navController.navigate(R.id.navigation_favourite)
                NavigationPage.ARCHIVE -> navController.navigate(R.id.navigation_archive)
                NavigationPage.ADD_NOTE -> navController.navigate(R.id.fragment_add_note).also { getPermissionForWindowOverlay() }
                NavigationPage.EDIT_NOTE -> navController.navigate(R.id.fragment_edit_note)
                NavigationPage.MAP -> navController.navigate(R.id.fragment_maps)
//                {
//                    when(navController.currentDestination?.id){
//                        R.id.fragment_add_note -> navController.navigate(R.id.action_addNote_to_maps)
//                        R.id.fragment_edit_note -> navController.navigate(R.id.action_editNote_to_maps)
//                        else->{}
//                    }
//                }
                //NavigationPage.ABOUT_US -> navController.navigate(R.id.na)
                //NavigationPage.HELP_SUPPORT -> navController.navigate(R.id.nav)
                else -> {}
            }
        })
        viewModel.showBottomNav.observe(this) {
            bottomAppBar.visibility = if (it) View.VISIBLE else View.GONE
            bottomNavView.visibility = if (it) View.VISIBLE else View.GONE
            var layoutParams = fragmentContainerView.layoutParams as MarginLayoutParams
            layoutParams.bottomMargin = if (it) actionBarSize() else 0
            if (it) {
                addFab.show()
            } else addFab.hide()
        }
        viewModel.title.observe(this) {
            titleView.text = it
        }
        viewModel.allNotes.observe(this) { it ->
            val list = arrayListOf<Pair<Long,Location>>()
            for (i in it.indices) {
                val note =it[i].toNoteModel()
                if(note.locationLat!=0.0 && note.locationLong!=0.0) {
                    val location = Location("")
                    location.latitude = note.locationLat
                    location.longitude = note.locationLong
                    list.add(Pair(note.id,location))

                }
            }
           // startLocationComparatorService(list)
            //update location list
            updateLocationList(list)
        }
    }

        private fun actionBarSize(): Int {
            val typedValue = TypedValue()
            // Resolve the ?attr/actionBarSize attribute to get the actual value
            theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)

            // Retrieve the actionBarSize value as a pixel dimension
            return resources.getDimensionPixelSize(typedValue.resourceId)
        }

        //LocationComparatorService starts here

    private fun startLocationComparatorService(mList:ArrayList<Int>) {
        val locationComparatorIntent = Intent(this, LocationComparatorService::class.java)
        locationComparatorIntent.putExtra("LIST",mList)
        //locationComparatorIntent.putExtra("CONTEXT",this as Parcelable)
        this.startService(locationComparatorIntent)
    }

    private fun updateLocationList(locationList:ArrayList<Pair<Long,Location>>) {
        if (locationList.isNotEmpty()) {
            //val list: List<Pair<Int, Location>> = locationList.toList()
            val locationList = locationList.map { pairToString(it) }.toTypedArray()
            Log.d(TAG,"location List = $locationList")
            val data = workDataOf(Constant.KEY_LOCATION_LIST to locationList)
            val periodicWorkRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
                repeatInterval = 3, TimeUnit.SECONDS
            ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED). build()).setInputData(data).build()
            //val oneTimeWorkRequest = OneTimeWorkRequestBuilder<LocationUpdateWorker>().setInputData(data).build()

            workManager.enqueue(periodicWorkRequest)

        }
    }
    fun pairToString(pair: Pair<Long, Location>): String {
        return "${pair.first},${pair.second.latitude},${pair.second.longitude}"
    }
    private fun getPermissionForWindowOverlay():Boolean{
        return if(Settings.canDrawOverlays(this)){
            Toast.makeText(this,"System overlay permission has been granted.",Toast.LENGTH_LONG).show()
            true
        }else{
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${this?.packageName}"))
            windowOverlayPermission.launch(intent)
            false
        }
    }
    private val windowOverlayPermission = registerForActivityResult( ActivityResultContracts.StartActivityForResult()){ result->
        if(Settings.canDrawOverlays(this)){
            Toast.makeText(this,"System overlay permission has been granted.",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this,"Sorry! this app is unable to display alarm details during active alarm alerts.",Toast.LENGTH_LONG).show()
        }

    }
}

