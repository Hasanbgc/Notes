package com.example.tempnavigation.views.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.DateUtil
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.FileUtil
import com.example.tempnavigation.utilities.PermissionUtils
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.IOException
import java.util.Calendar


class AddNoteFragment : Fragment(),View.OnClickListener, Dialogs by DialogUtils(),OnMapReadyCallback{

    //region variable
    private var TAG = "AddNoteFragment"
    private val mainViewModel: MainViewModel by activityViewModels()
    private  val addNoteFragmentViewModel: AddNoteFragmentViewModel by viewModels()
    private lateinit var permissionUtils:PermissionUtils

    private  var rootView:View? = null
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var imageView:ImageView
    private lateinit var discardButton:ImageView
    private lateinit var imageFab:FloatingActionButton
    private lateinit var cameraFab:FloatingActionButton
    private lateinit var locationFab:FloatingActionButton
    private lateinit var reminderFab:FloatingActionButton
    private lateinit var menuItem: Menu
    private lateinit var imageHolder:CardView
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var upButton: ImageView
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var bottomSheetClock: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var textClock: TextClock
    private lateinit var mTimePicker: MaterialTimePicker
    private lateinit var dialogs : View
    private lateinit var clockDialog: View
    private lateinit var timePicker: MaterialTimePicker
    private lateinit var locationOfBottomSheet:LinearLayout
    private lateinit var imagePickerOfBottomSheet: LinearLayout
    private lateinit var cameraOfBottomSheet: LinearLayout
    private lateinit var timePickerOfBottomSheet: LinearLayout
    private lateinit var mapView:MapView
    private lateinit var discardButtonMap:ImageView
    private lateinit var mapHolder:CardView
    private lateinit var googleMap: GoogleMap

    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
    private val storagePermission33 = arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO)
    private val  cameraPermission = arrayOf(Manifest.permission.CAMERA)
    private var uriForCamera: Uri = Uri.EMPTY
    private var imageTitle = ""
    private  var imageUri:String = ""
    private lateinit var bitmap:Bitmap
    private var permissionRequestedByCamera = false
    private var saved = false
    private var mapVisibility = false
    private var previousTitle=""
    private var currentTitle=""
    //endregion
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_add_note, container, false)
        inputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        setHasOptionsMenu(true)

        initView()
        setInitValue()
        //setAddMenuVisibility()
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return rootView
    }

    override fun onResume() {
        super.onResume()
        setAddMenuVisibility()
        mapView.onResume()

    }
    private fun initView(){
        editTextTitle = rootView?.findViewById(R.id.editText_title)!!
        editTextDescription = rootView!!.findViewById(R.id.editText_description)
        imageFab = rootView!!.findViewById(R.id.fab_image_id)
        cameraFab = rootView!!.findViewById(R.id.fab_camera)
        upButton = rootView!!.findViewById(R.id.image_view_up)
        locationFab = rootView!!.findViewById(R.id.fab_location_id)
        reminderFab = rootView!!.findViewById(R.id.fab_reminder_id)
        imageView = rootView!!.findViewById(R.id.image_view)
        discardButton = rootView!!.findViewById(R.id.button_discard)
        imageHolder = rootView!!.findViewById(R.id.image_holder)
        mapView = rootView!!.findViewById(R.id.map_view_addNote)

        mapHolder = rootView!!.findViewById(R.id.map_holder)
        discardButtonMap = rootView!!.findViewById(R.id.button_discard_map)
        cameraFab.setOnClickListener(this)
        imageFab.setOnClickListener(this)
        locationFab.setOnClickListener(this)
        discardButton.setOnClickListener(this)
        discardButtonMap.setOnClickListener(this)
        upButton.setOnClickListener(this)

        clockDialog = layoutInflater.inflate(R.layout.timer_bottom_sheet,null)
        dialogs = layoutInflater.inflate(R.layout.bottom_sheet,null)
        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(dialogs)
        locationOfBottomSheet = dialogs.findViewById(R.id.location)
        timePickerOfBottomSheet = dialogs.findViewById(R.id.reminder)
        imagePickerOfBottomSheet = dialogs.findViewById(R.id.image)
        cameraOfBottomSheet = dialogs.findViewById(R.id.camera)
        locationOfBottomSheet.setOnClickListener(this)
        timePickerOfBottomSheet.setOnClickListener(this)
        imagePickerOfBottomSheet.setOnClickListener(this)
        cameraOfBottomSheet.setOnClickListener(this)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save-> performNoteSave()
            R.id.delete-> clear()
            android.R.id.home-> {
                hideSoftKeyboard()
                performNoteSave()
                mainViewModel.navigationPage.value = NavigationPage.HOME
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_note_menu,menu)
        menuItem = menu
    }
    private fun setAddMenuVisibility(){
        editTextTitle.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = editTextTitle.text.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = editTextTitle.text.isNotEmpty()
        }
        editTextDescription.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = editTextDescription.text!!.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = editTextDescription.text!!.isNotEmpty()
        }
    }

    private fun setInitValue(){
        mainViewModel.showBottomNav.value = false
        mainViewModel.title.value = "Add Note"
        permissionUtils = PermissionUtils(requireActivity())
    }
    private fun clear(){
        if (editTextTitle.hasFocus()){
            editTextTitle.text.clear()
        }else{
            editTextDescription.text?.clear()
        }
    }
    private fun performNoteSave(){
        previousTitle = currentTitle
        currentTitle = editTextTitle.text.trim().toString()
        val description = editTextDescription.text?.trim().toString()
        val imgUri = imageUri.toString()
        if(currentTitle.isNotEmpty() && description.isNotEmpty()){
            menuItem.findItem(R.id.save).isEnabled = true
        }
        val note = NoteModel(0,currentTitle,description,1,imgUri)
        if(saved){
            updateNote(note)
        }else{
            insertNote(note)
        }
    }
    private fun updateNote(currentNote:NoteModel){
       addNoteFragmentViewModel.getNoteByTitle(previousTitle,{savedNote ->
           savedNote.title = currentNote.title
           savedNote.description = currentNote.description
           savedNote.priority = currentNote.priority
           savedNote.imageUri = currentNote.imageUri
           addNoteFragmentViewModel.update(savedNote,{},{})
       },{})

    }
    private fun insertNote(note:NoteModel) {
        addNoteFragmentViewModel.insert(note, onSuccess = {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "note Saved", Toast.LENGTH_SHORT).show()
            }
            saved = true
        }, onFailed = {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun hideSoftKeyboard(): Boolean {
        try {
           requireActivity().currentFocus?.let {
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView=null
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.image->{
                bottomSheet.dismiss()
                permissionRequestedByCamera = false
                when{
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> openResultFragment()
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasMediaAccessPermission(
                        requireActivity(),
                        {openResultFragment()},
                        {requestPermissionLaunch(permissionUtils.permissionForThisApiVersion())})
                    else->{}
                }
            }
            R.id.camera->{
                bottomSheet.dismiss()
                permissionRequestedByCamera = true
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasCameraAccessPermission(requireActivity(), onPermissonGranted = { openCamera()
                        Toast.makeText(context,"opened by Fab Camera[0]",Toast.LENGTH_SHORT).show()
                        Log.d(TAG,"opened by Fab Camera[0]")
                    }, requestPermission = {requestPermissionLaunch(cameraPermission)})
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasCameraAccessPermission(requireActivity(),
                        onPermissonGranted = { permissionUtils.hasMediaAccessPermission(requireActivity(), onPermissonGranted = {openCamera()},
                            requestPermission = {requestPermissionLaunch(permissionUtils.permissionForThisApiVersion())})
                        Toast.makeText(context,"opened by Fab Camera[1]",Toast.LENGTH_SHORT).show()
                        Log.d(TAG,"opened by Fab Camera[1]")
                    },{requestPermissionLaunch(cameraPermission)})
                    else -> {}
                }
            }
            R.id.button_discard->{
                if (uriForCamera != Uri.EMPTY) {
                    deleteTempImage(uriForCamera)
                }
                imageView.setImageResource(0)
                imageHolder.visibility = View.GONE
            }
            R.id.button_discard_map->{
                mapHolder.visibility = View.GONE
            }
            R.id.location->{
                bottomSheet.dismiss()
                permissionUtils.hasLocationPermission(requireActivity(),
                    {   permissionUtils.hasBgLocationPermission(requireActivity(),{},{requestPermissionLaunch(permissionUtils.bgLocationPermission())})
                        mainViewModel.navigationPage.value = NavigationPage.MAP
                    },{requestPermissionLaunch(permissionUtils.locationPermission())})
                mapVisibility = true
            }
            R.id.reminder ->{
                bottomSheet.dismiss()
                loadMaterialTimePicker()
                fragmentManager?.let { it1 -> mTimePicker.show(it1,TAG) }
            }
            R.id.image_view_up->{
                bottomSheet.show()
            }
        }
    }
    private fun getMapsApiKey(): String = "AIzaSyCXeiuE3k90dDZjS15aozfn1qMKk5U0SyU"

    private fun loadMaterialTimePicker(){
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        mTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setPositiveButtonText("Ok")
            .setNegativeButtonText("Cancel")
            .build()
        mTimePicker.addOnPositiveButtonClickListener(View.OnClickListener {
            val am_pm = if(mTimePicker.hour>=12)"PM" else "AM"
            val hr =if (mTimePicker.hour>12) mTimePicker.hour - 12 else mTimePicker.hour
           // textClock.setText("${hr}:${mTimePicker.minute} $am_pm")
            mTimePicker.dismiss()
        })
        mTimePicker.addOnNegativeButtonClickListener {
            mTimePicker.dismiss()
        }
    }
    //region photo picker

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if(uri!= null) {
            imageView.setImageURI(uri)
            imageUri = uri.toString()
            imageHolder.visibility = View.VISIBLE
        }
    }
    private fun openResultFragment() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    //endregion

    //region camera
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                //bitmap = result.data?.extras?.get
                bitmap = ((result.data?.extras?.get("data") as? Bitmap)!!)
                imageHolder.visibility = View.VISIBLE
                imageView.setImageBitmap(bitmap)
                imageTitle = DateUtil.getCurrentTime().toString()
                imageUri =
                    FileUtil.saveBitmapToAppFolderAndGetPath(requireContext(),bitmap,imageTitle).toString()
                Log.d(TAG,imageUri)
                Toast.makeText(context,imageUri,Toast.LENGTH_LONG).show()

            }catch (e:IOException){
                e.printStackTrace()
            }
        }else{
            Toast.makeText(requireContext(),"no image captured",Toast.LENGTH_SHORT).show()
        }
    }
    private fun openCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(cameraIntent)
    }
    //endregion

    //region Permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions->
        permissions.forEach{actionMap->
            when(actionMap.key){
                cameraPermission[0]->{
                    if (actionMap.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                         openCamera() //should open for camera
                        Toast.makeText(context,"opened by camera permission",Toast.LENGTH_SHORT).show()
                    }else{
                        permissionUtils.hasMediaAccessPermission(requireActivity(),{openCamera()},{requestPermissionLaunch(permissionUtils.permissionForThisApiVersion())})
                    }
                }
                //image
                permissionUtils.permissionForThisApiVersion()[0]->{
                    if (actionMap.value){
                        if(permissionRequestedByCamera){
                            openCamera()
                            Toast.makeText(context,"opened by permissionForApiVersion()[0]",Toast.LENGTH_SHORT).show()
                            Log.d(TAG,"opened by permissionForApiVersion()[0]")
                        }else {
                            openResultFragment()
                        }
                    }else{
                        permissionUtils.hasMediaAccessPermission(requireActivity(),{openCamera()},{requestPermissionLaunch(permissionUtils.permissionForThisApiVersion())})

                    }
                }
                //audio
                permissionUtils.permissionForThisApiVersion()[1]->{
                    if (actionMap.value){
                        if(permissionRequestedByCamera){
                            //openCamera()
                            Toast.makeText(context,"opened by permissionForApiVersion()[1]",Toast.LENGTH_SHORT).show()
                            Log.d(TAG,"opened by permissionForApiVersion()[1]")
                        }else {
                           // openResultFragment()
                        } //should open for audio
                    }else{
                        permissionUtils.hasMediaAccessPermission(requireActivity(),{openCamera()},{requestPermissionLaunch(permissionUtils.permissionForThisApiVersion())})

                    }
                }
                //Location
                permissionUtils.locationPermission()[0] -> {
                    ///TODO(need to handle the don't ask again in permission)
                    if (actionMap.value) {
                        //getfusedLocation
                        permissionUtils.hasBgLocationPermission(requireActivity(),{},{
                        doubleButtonDialog(
                            requireActivity(),
                            getString(R.string.bg_location_title),
                            getString(R.string.education_massage_for_bg_location),
                            "Yes",
                            "No",
                            {
                                requestPermissionLaunch(permissionUtils.bgLocationPermission())
                            },
                            {})
                        //Toast.makeText(requireContext(),"location permission = Fine location",Toast.LENGTH_SHORT).show()
                        })
                        mainViewModel.navigationPage.value = NavigationPage.MAP
                    }
                }
                permissionUtils.bgLocationPermission()[0]->{
                    if(actionMap.value){
                        Toast.makeText(requireContext(),"location permission = background Location",Toast.LENGTH_SHORT).show()
                        //getLocation()
                    }
                }
            }
        }

    }

//    private fun getCapturedImage(capturedImageUri: Uri): Bitmap {
//        return when {
//            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(requireContext().contentResolver, capturedImageUri)
//            else -> {
//                val source = ImageDecoder.createSource(requireContext().contentResolver, capturedImageUri)
//                ImageDecoder.decodeBitmap(source)
//            }
//        }
//    }
    // Delete an image from the given URI
    private fun deleteTempImage(capturedImageUri: Uri){
        val contentResolver = context?.contentResolver
            val selection = "${MediaStore.MediaColumns._ID} = ?"
            val selectionArgs = arrayOf(capturedImageUri.lastPathSegment)

            contentResolver?.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs)

    }

    private fun requestPermissionLaunch(requests:Array<String>) {
        //Toast.makeText(requireContext(), "i am being called", Toast.LENGTH_SHORT).show()
        requestPermissionLauncher.launch(requests)
            //requestPermissionLauncher(permissionForApiVersion(),Constant.REQUEST_WRITE_EXTERNAL_STORAGE)
    }
    //endregion
    override fun onMapReady(map: GoogleMap) {
        var lat = 0.0;
        var long = 0.0
        googleMap = map
        mainViewModel.selectedLocation.observe(viewLifecycleOwner, Observer { location ->
            lat = location.first
            long = location.second
        })
        val location = LatLng(lat, long)
        googleMap.addMarker(MarkerOptions().position(location))

        // Move the camera to the selected location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
        if(lat != 0.0 && long != 0.0 && mapVisibility) {
            Log.d(TAG,"init location = $lat,$long")
            mapHolder.visibility = View.VISIBLE
            //mapVisibility = true
        }
        // Disable marker dragging
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false

    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}