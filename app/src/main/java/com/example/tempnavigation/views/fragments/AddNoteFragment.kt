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
import android.provider.Settings
import android.text.InputType
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
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.tempnavigation.R
import com.example.tempnavigation.models.AlarmData
import com.example.tempnavigation.alarms.AlarmSchedulersImplementation
import com.example.tempnavigation.databinding.BottomSheetBinding
import com.example.tempnavigation.databinding.FragmentAddNoteBinding
import com.example.tempnavigation.helpers.GeoFancingManger
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.room.entity.NoteEntity
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.wada811.viewbindingktx.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddNoteFragment : Fragment(R.layout.fragment_add_note), View.OnClickListener, Dialogs by DialogUtils(), OnMapReadyCallback {

    //region variable
    private var TAG = "AddNoteFragment"
    private val binding by viewBinding(FragmentAddNoteBinding::bind)
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: AddNoteFragmentViewModel by viewModels()
    private lateinit var permissionUtils: PermissionUtils
    @Inject lateinit var geoFancingManger: GeoFancingManger

    private var rootView: View? = null
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var imageView: ImageView
    private lateinit var discardButton: ImageView
    private lateinit var menuItem: Menu
    private lateinit var imageHolder: CardView
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var upButton: ImageView
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var bottomSheetClock: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var textClock: TextClock
    private lateinit var mTimePicker: MaterialTimePicker
    private lateinit var dialogs: View
    private lateinit var clockDialog: View
    private lateinit var timePicker: MaterialTimePicker
    private lateinit var locationOfBottomSheet: LinearLayout
    private lateinit var imagePickerOfBottomSheet: LinearLayout
    private lateinit var cameraOfBottomSheet: LinearLayout
    private lateinit var timePickerOfBottomSheet: LinearLayout
    private lateinit var mapView: MapView
    private lateinit var discardButtonMap: ImageView
    private lateinit var mapHolder: CardView
    private lateinit var googleMap: GoogleMap
    private lateinit var switch: SwitchCompat

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var alarmTime: String
    private var savedTime: Long = 0
    private var id: String = ""
    private var favourite = false
    private var archive = false


    private val storagePermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val storagePermission33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )
    private val cameraPermission = arrayOf(Manifest.permission.CAMERA)
    private var uriForCamera: Uri = Uri.EMPTY
    private var imageTitle = ""
    private var imageUri: String = ""
    private lateinit var bitmap: Bitmap
    private var permissionRequestedByCamera = false
    private var saved = false
    private var mapVisibility = false
    private var previousTitle = ""
    private var currentTitle = ""
    private var location = Pair(0.0, 0.0)
    private var timeInLocal = ""
    private lateinit var alarmSchedulersImplementation: AlarmSchedulersImplementation
    private var bottomSheetBinding: BottomSheetBinding? = null

    //endregion
    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingAddNoteFragment = FragmentAddNoteBinding.inflate(inflater, container, false)
        rootView = bindingAddNoteFragment?.root
        inputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager


        return rootView
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        setHasOptionsMenu(true)

        initView()
        setInitValue()
        observeLiveData()
        //setAddMenuVisibility()
        binding.mapViewAddNote.onCreate(savedInstanceState)
        binding.mapViewAddNote.getMapAsync(this)

    }

    override fun onResume() {
        super.onResume()
        setAddMenuVisibility()
        requireActivity().invalidateOptionsMenu()
        binding.mapViewAddNote.onResume()

    }

    private fun initView() {
        binding.apply {
            editTextTitle.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            editTextDescription.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            buttonDiscard.setOnClickListener(this@AddNoteFragment)
            buttonDiscardMap.setOnClickListener(this@AddNoteFragment)
            imageViewUp.setOnClickListener(this@AddNoteFragment)
        }

       /* upButton = binding.imageViewUp
        imageView = binding.imageView
        discardButton = binding.buttonDiscard
        imageHolder = binding.imageHolder
        mapView = binding.mapViewAddNote*/

        //mapHolder = binding.mapHolder
        bottomSheet = BottomSheetDialog(requireContext())
        bottomSheetBinding = BottomSheetBinding.inflate(layoutInflater)


        locationOfBottomSheet = bottomSheetBinding?.location!!
        timePickerOfBottomSheet = bottomSheetBinding?.reminder!!
        imagePickerOfBottomSheet = bottomSheetBinding?.image!!
        cameraOfBottomSheet = bottomSheetBinding?.camera!!
        locationOfBottomSheet.setOnClickListener(this)
        timePickerOfBottomSheet.setOnClickListener(this)
        imagePickerOfBottomSheet.setOnClickListener(this)
        cameraOfBottomSheet.setOnClickListener(this)

        bottomSheetBinding?.root?.let { bottomSheet.setContentView(it) }
    }

    private fun observeLiveData() {
        mainViewModel.selectedNote.observe(viewLifecycleOwner, Observer { noteModel ->
            Log.d(TAG, "observeLiveData: $noteModel")
            viewModel.currentNote = noteModel
            //id = noteModel.id
            //title = noteModel.title
            //description = noteModel.description
            //imageUri = noteModel.imageUri
            location = Pair(noteModel.locationLat, noteModel.locationLong)
            Log.d(TAG, "location: $location")
            //alarmTime = noteModel.alarmTime
            //savedTime = noteModel.savedTime
            //favourite = noteModel.favourite

            binding.editTextTitle.setText(noteModel.title)
            binding.editTextDescription.setText(noteModel.description)

            if (noteModel.imageUri != "") {
                binding.imageHolder.visibility = View.VISIBLE
                Glide.with(this).load(imageUri).into(imageView)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> performNoteSave()
            R.id.delete -> clear()
            android.R.id.home -> {
                hideSoftKeyboard()
                performNoteSave()
                mainViewModel.selectedNote.value = NoteModel.emptyNote()
                mainViewModel.navigationPage.value = NavigationPage.HOME

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.add_note_menu, menu)
        menuItem = menu

        //Switch on the title bar to alarm on off
//        val switchItem = menuItem.findItem(R.id.switchItem)
//        val switchViewLayout = switchItem.actionView as FrameLayout
//        switch = switchViewLayout.findViewById(R.id.switchView) as SwitchCompat
//        switch.visibility = View.GONE
//        //alarmOnOff()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setAddMenuVisibility() {
        binding.editTextTitle.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = binding.editTextTitle.text.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = binding.editTextTitle.text.isNotEmpty()
        }
        binding.editTextDescription.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = binding.editTextDescription.text!!.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = binding.editTextDescription.text!!.isNotEmpty()
        }
    }

    private fun setInitValue() {
        mainViewModel.showBottomNav.value = false
        mainViewModel.title.value = "Add Note"
        permissionUtils = PermissionUtils(requireActivity())
        alarmSchedulersImplementation = AlarmSchedulersImplementation(requireContext())
    }

    private fun alarmOn(title: String, message: String) {
        alarmSchedulersImplementation.schedule(AlarmData(timeInLocal, title, message))
    }

    private fun clear() {
        if (binding.editTextTitle.hasFocus()) {
            binding.editTextTitle.text.clear()
        } else {
            binding.editTextDescription.text?.clear()
        }
    }

//    private fun performNoteSave() {
//
//        val noteModel = validateNote()
//        Log.d(TAG, "performNoteSave: $noteModel")
//        if (noteModel.isEmpty()) {
//            if (saved) {
//                updateNote(noteModel)
//                Log.d(TAG, "updated note: $noteModel,prev:$previousTitle")
//            } else {
//                insertNote(noteModel)
//            }
//        } else {
//            DialogUtils.toast(requireContext(), "Blank note can not be saved.")
//        }
//
//    }

    fun performNoteSave() {
        if (validateNote()) {
            val note = NoteModel(
                viewModel.currentNote.id,
                binding.editTextTitle.text.toString().trim(),
                binding.editTextDescription.text.toString().trim(),
                location.first,
                location.second,
                imageUri,
                timeInLocal,
                DateUtil.getCurrentTime(),
                viewModel.currentNote.favourite,
                viewModel.currentNote.archive
            )
            if(viewModel.currentNote.savedTime<=0L) {
                insertNote(note)
            }else{
                updateNote(note)
            }
        } else {
            DialogUtils.toast(requireContext(), "Blank note can not be saved.")
        }
    }

    private fun validateNote(): Boolean {
        title = binding.editTextTitle.text.toString().trim()
        description = binding.editTextDescription.text.toString().trim()
        imageUri = viewModel.getImageUri()

        // Check if the note has any content
        val hasContent = title.isNotEmpty() ||
                description.isNotEmpty() ||
                imageUri.isNotEmpty() ||
                (location.first!=0.0 && location.second!=0.0) ||
                timeInLocal.isNotEmpty()

        if (!hasContent) return false


        // Schedule alarm if alarm time is set
        if (timeInLocal.isNotEmpty()) {
            alarmOn(title, description)
        }

        return true
    }

    private fun updateNote(currentNote: NoteModel) {
        Log.d(TAG, "updateNote: $previousTitle")
        viewModel.update(currentNote, {status->
            if(status)
                Handler(Looper.getMainLooper()).post {
                    DialogUtils.toast(requireContext(), "Note Updated.")
                }
        }, {})

    }

    private fun insertNote(note: NoteModel) {
        viewModel.insert(note, onSuccess = {msg->
            requireActivity().runOnUiThread {
                updateUI(msg)
            }
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
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.image -> {
                bottomSheet.dismiss()
                permissionRequestedByCamera = false
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> openResultFragment()
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasMediaAccessPermission(
                        requireActivity(),
                        { openResultFragment() },
                        { requestPermissionLaunch(permissionUtils.permissionForThisApiVersion()) })

                    else -> {}
                }
            }

            R.id.camera -> {
                bottomSheet.dismiss()
                permissionRequestedByCamera = true
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasCameraAccessPermission(
                        requireActivity(),
                        onPermissonGranted = {
                            openCamera()
                            Toast.makeText(context, "opened by Fab Camera[0]", Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, "opened by Fab Camera[0]")
                        },
                        requestPermission = { requestPermissionLaunch(cameraPermission) })

                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> permissionUtils.hasCameraAccessPermission(
                        requireActivity(),
                        onPermissonGranted = {
                            permissionUtils.hasMediaAccessPermission(requireActivity(),
                                onPermissonGranted = { openCamera() },
                                requestPermission = { requestPermissionLaunch(permissionUtils.permissionForThisApiVersion()) })
                            Toast.makeText(context, "opened by Fab Camera[1]", Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, "opened by Fab Camera[1]")
                        },
                        { requestPermissionLaunch(cameraPermission) })

                    else -> {}
                }
            }

            R.id.button_discard -> {
                if (uriForCamera != Uri.EMPTY) {
                    deleteTempImage(uriForCamera)
                }
                binding.imageView.setImageResource(0)
                viewModel.setImageUri("")
                updateImageVisibility()
            }

            R.id.button_discard_map -> {
                binding.mapHolder.visibility = View.GONE
            }

            R.id.location -> {
                bottomSheet.dismiss()
                permissionUtils.hasLocationPermission(requireActivity(),
                    {
                        permissionUtils.hasBgLocationPermission(requireActivity(),
                            {},
                            { requestPermissionLaunch(permissionUtils.bgLocationPermission()) })
                        mainViewModel.navigationPage.value = NavigationPage.MAP
                    }, { requestPermissionLaunch(permissionUtils.locationPermission()) })
                getPermissionForWindowOverlay()
                mapVisibility = true
            }

            R.id.reminder -> {
                bottomSheet.dismiss()
                getPermissionForWindowOverlay()
                loadMaterialTimePicker()
                // fragmentManager?.let { it1 -> mTimePicker.show(it1,TAG) }
            }

            R.id.image_view_up -> {
                bottomSheet.show()
            }
        }
    }

    private fun loadMaterialTimePicker() {
        val fragmentManger = requireActivity().supportFragmentManager
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
        mTimePicker.show(fragmentManger, "AddNoteFragment")
        mTimePicker.addOnPositiveButtonClickListener(View.OnClickListener {
            if (mTimePicker.hour >= 12) "PM" else "AM"
            if (mTimePicker.hour > 12) mTimePicker.hour - 12 else mTimePicker.hour
            timeInLocal = convertToLocalizedTime(mTimePicker.hour, mTimePicker.minute)
            //alarmSchedulersImplementation.schedule(AlarmData(timeInLocal,"fire this alarm"))
            mTimePicker.dismiss()
//            switch.visibility = View.VISIBLE
        })
        mTimePicker.addOnNegativeButtonClickListener {
            mTimePicker.dismiss()
            // switch.visibility = View.GONE
        }
    }

    private fun convertToLocalizedTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val localeTime = dateFormat.format(calendar.time)
        Log.d(TAG, localeTime.toString())
        return localeTime
    }
    //region photo picker

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                binding.imageView.setImageURI(uri)
                viewModel.setImageUri(uri.toString())
                binding.imageHolder.visibility = View.VISIBLE
                val imagePath = uri.path
                Toast.makeText(requireContext(), "image path = $imagePath", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "image path: $imagePath")
            }
        }

    private fun openResultFragment() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    //endregion

    //region camera
    private val captureImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    //bitmap = result.data?.extras?.get
                    bitmap = result.data?.extras?.get("data") as Bitmap
                    imageTitle = DateUtil.getCurrentTime().toString()
                    imageUri =
                        FileUtil.saveBitmapToAppFolderAndGetPath(
                            requireContext(),
                            bitmap,
                            imageTitle
                        ).toString()
                    viewModel.setImageUri(imageUri)
                    updateImageVisibility()
                    Log.d(TAG, imageUri)
                    Toast.makeText(context, imageUri, Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
//            }finally {
//                if (bitmap != null){
//                    bitmap.recycle()
//                }
//            }
            } else {
                Toast.makeText(requireContext(), "no image captured", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(cameraIntent)
    }
    //endregion

    //region Permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { actionMap ->
                when (actionMap.key) {
                    cameraPermission[0] -> {
                        if (actionMap.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            openCamera() //should open for camera
                            Toast.makeText(
                                context,
                                "opened by camera permission",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            permissionUtils.hasMediaAccessPermission(requireActivity(),
                                { openCamera() },
                                { requestPermissionLaunch(permissionUtils.permissionForThisApiVersion()) })
                        }
                    }
                    //image
                    permissionUtils.permissionForThisApiVersion()[0] -> {
                        if (actionMap.value) {
                            if (permissionRequestedByCamera) {
                                openCamera()
                                Toast.makeText(
                                    context,
                                    "opened by permissionForApiVersion()[0]",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(TAG, "opened by permissionForApiVersion()[0]")
                            } else {
                                openResultFragment()
                            }
                        } else {
                            permissionUtils.hasMediaAccessPermission(requireActivity(),
                                { openCamera() },
                                { requestPermissionLaunch(permissionUtils.permissionForThisApiVersion()) })

                        }
                    }
                    //audio
                    permissionUtils.permissionForThisApiVersion()[1] -> {
                        if (actionMap.value) {
                            if (permissionRequestedByCamera) {
                                //openCamera()
                                Toast.makeText(
                                    context,
                                    "opened by permissionForApiVersion()[1]",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(TAG, "opened by permissionForApiVersion()[1]")
                            } else {
                                // openResultFragment()
                            } //should open for audio
                        } else {
                            permissionUtils.hasMediaAccessPermission(requireActivity(),
                                { openCamera() },
                                { requestPermissionLaunch(permissionUtils.permissionForThisApiVersion()) })

                        }
                    }
                    //Location
                    permissionUtils.locationPermission()[0] -> {
                        ///TODO(need to handle the don't ask again in permission)
                        if (actionMap.value) {
                            //getfusedLocation
                            permissionUtils.hasBgLocationPermission(requireActivity(), {}, {
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

                    permissionUtils.bgLocationPermission()[0] -> {
                        if (actionMap.value) {
                            Toast.makeText(
                                requireContext(),
                                "location permission = background Location",
                                Toast.LENGTH_SHORT
                            ).show()
                            //getLocation()
                        }
                    }
                }
            }

        }

    private val windowOverlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(context)) {

            } else {
                Toast.makeText(
                    context,
                    "Sorry! this app is unable to display alarm details during active alarm alerts.",
                    Toast.LENGTH_LONG
                ).show()
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
    private fun deleteTempImage(capturedImageUri: Uri) {
        val contentResolver = context?.contentResolver
        val selection = "${MediaStore.MediaColumns._ID} = ?"
        val selectionArgs = arrayOf(capturedImageUri.lastPathSegment)

        contentResolver?.delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            selection,
            selectionArgs
        )

    }

    private fun requestPermissionLaunch(requests: Array<String>) {
        //Toast.makeText(requireContext(), "i am being called", Toast.LENGTH_SHORT).show()
        requestPermissionLauncher.launch(requests)
        //requestPermissionLauncher(permissionForApiVersion(),Constant.REQUEST_WRITE_EXTERNAL_STORAGE)
    }

    //endregion
    override fun onMapReady(map: GoogleMap) {
        var lat = 0.0
        var long = 0.0
        googleMap = map
        mainViewModel.selectedLocation.observe(viewLifecycleOwner, Observer { location ->
            lat = location.first
            long = location.second
            this.location = location
        })
        val locationLatLng = LatLng(lat, long)
        googleMap.addMarker(MarkerOptions().position(locationLatLng))

        // Move the camera to the selected location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 20f))
        if (lat != 0.0 && long != 0.0 && mapVisibility) {
            Log.d(TAG, "init location = $lat,$long")
            binding.mapHolder.visibility = View.VISIBLE
            updateImageVisibility()
        }
        // Disable marker dragging
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false

    }

    //window overlay permission for system alert regarding alarm dialog notification
    private fun getPermissionForWindowOverlay(): Boolean {
        return if (Settings.canDrawOverlays(context)) {
            true
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context?.packageName}")
            )
            windowOverlayPermission.launch(intent)
            false
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().invalidateOptionsMenu()
        binding.mapViewAddNote.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapViewAddNote.onDestroy()

    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapViewAddNote.onLowMemory()
    }

    fun updateImageVisibility() {
        val imagePath = viewModel.getImageUri()
        if (imagePath.isNotEmpty()) {
            binding.imageHolder.visibility = View.VISIBLE
            binding.imageView.setImageURI(imagePath.toUri())
        } else {
            binding.imageHolder.visibility = View.GONE
        }
    }

    fun updateUI(msg:String){
        DialogUtils.toast(requireContext(),msg)
    }

}