package com.example.tempnavigation.views

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.example.tempnavigation.R
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), View.OnClickListener, Dialogs by DialogUtils() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavigation()
        initWindow()
        initView()
        observeLiveData()
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
                }
                else{
                    Toast.makeText(this,"backpressed clicked",Toast.LENGTH_SHORT).show()
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
                NavigationPage.ADD_NOTE -> navController.navigate(R.id.fragment_add_note)
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
    }

    private fun actionBarSize(): Int {
        val typedValue = TypedValue()
        // Resolve the ?attr/actionBarSize attribute to get the actual value
        theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)

        // Retrieve the actionBarSize value as a pixel dimension
        return resources.getDimensionPixelSize(typedValue.resourceId)
    }
}