package com.example.tempnavigation.views.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.tempnavigation.R
import com.example.tempnavigation.utilities.DateUtil
import com.example.tempnavigation.utilities.DialogUtils
import com.example.tempnavigation.utilities.Dialogs
import com.example.tempnavigation.utilities.FileUtil
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.IOException


class AddNoteFragment : Fragment(),View.OnClickListener, Dialogs by DialogUtils(){

    private var TAG = "AddNoteFragment"
    private val mainViewModel: MainViewModel by activityViewModels()
    private  val addNoteFragmentViewModel: AddNoteFragmentViewModel by viewModels()

    private  var rootView:View? = null
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var imageView:ImageView
    private lateinit var discardButton:ImageView
    private lateinit var imageFab:FloatingActionButton
    private lateinit var cameraFab:FloatingActionButton
    private lateinit var menuItem: Menu
    private lateinit var imageHolder:CardView
    private lateinit var inputMethodManager: InputMethodManager

    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
    private val storagePermission33 = arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO)
    private val  cameraPermission = arrayOf(Manifest.permission.CAMERA)
    private lateinit var uriForCamera: Uri
    private lateinit var imageTitle:String
    private lateinit var savedImageFile:String
    private var permissionRequestedByCamera = false

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
        setAddMenuVisibility()

        return rootView
    }

    private fun initView(){
        editTextTitle = rootView?.findViewById(R.id.editText_title)!!
        editTextDescription = rootView!!.findViewById(R.id.editText_description)
        imageFab = rootView!!.findViewById(R.id.fab_image_id)
        cameraFab = rootView!!.findViewById(R.id.fab_camera)
        imageView = rootView!!.findViewById(R.id.image_view)
        discardButton = rootView!!.findViewById(R.id.button_discard)
        imageHolder = rootView!!.findViewById(R.id.image_holder)
        cameraFab.setOnClickListener(this)
        imageFab.setOnClickListener(this)
        discardButton.setOnClickListener(this)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save-> saveNote()
            R.id.delete-> clear()
            android.R.id.home-> {
                hideSoftKeyboard()
                if (editTextTitle.text.isNotEmpty() && editTextDescription.text?.isNotEmpty() == true){
                saveNote()}
                mainViewModel.navigationPage.value = NavigationPage.HOME
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_note_menu,menu)
        menuItem = menu
        super.onCreateOptionsMenu(menu, inflater)
    }
    private fun setAddMenuVisibility(){
        editTextTitle.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = editTextTitle.text.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = true
        }
    }
    private fun setInitValue(){
        mainViewModel.showBottomNav.value = false
        mainViewModel.title.value = "Add Note"
    }
    private fun clear(){
        if (editTextTitle.hasFocus()){
            editTextTitle.text.clear()
        }else{
            editTextDescription.text?.clear()
        }
    }
    private fun saveNote(){
        val title = editTextTitle.text.trim().toString()
        val description = editTextDescription.text?.trim().toString()
        if(title.isNotEmpty() && description.isNotEmpty()){
            menuItem.findItem(R.id.save).isEnabled = true
        }
        addNoteFragmentViewModel.insert(title,description,1, onSuccess = {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(),"note Saved",Toast.LENGTH_SHORT).show()
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
        rootView=null
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fab_image_id->{
                permissionRequestedByCamera = false
                when{
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> openResultFragment()
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> checkMediaAccessPermission(){openResultFragment()}
                    else->{}
                }
            }
            R.id.fab_camera->{
                permissionRequestedByCamera = true
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> checkCameraPermission { openCamera() }
                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU -> checkCameraPermission { checkMediaAccessPermission(){openCamera()} }
                    else -> {}
                }
            }
            R.id.button_discard->{
                imageView.setImageResource(0)
                imageHolder.visibility = View.GONE
            }
        }
    }


    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            imageView.setImageURI(uri)
            imageHolder.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openResultFragment() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val bitmap = getCapturedImage(uriForCamera)
                savedImageFile =  FileUtil.saveImage(requireContext(),imageTitle,bitmap)
                Toast.makeText(context,savedImageFile,Toast.LENGTH_LONG).show()
                Log.d(TAG,savedImageFile)
            }catch (e:IOException){
                e.printStackTrace()
            }
            imageHolder.visibility = View.VISIBLE
            val bitmap = FileUtil.getImageFromInternalStorage(savedImageFile)
            imageView.setImageBitmap(bitmap)
        }else{
            Toast.makeText(requireContext(),"no image captured",Toast.LENGTH_SHORT).show()
        }
    }
    private fun openCamera(){
        val contentValues = ContentValues()
        imageTitle = DateUtil.getCurrentTime().toString()+".png"
        contentValues.put(MediaStore.Images.Media.TITLE,imageTitle)
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"captured by hasan")
        uriForCamera = requireContext().contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)!!



        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,uriForCamera)
        captureImage.launch(cameraIntent)
    }


    //region Permission

    private fun permissionForApiVersion():Array<String>{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            storagePermission33
        }else{
            storagePermission
        }
    }
    private fun checkMediaAccessPermission(onSuccess: () -> Unit){
        when{
            isCheckSelfPermissionGranted(requireActivity())->{
                onSuccess()
            }
            isPermissionRational(requireActivity())->{
                doubleButtonDialog(requireActivity(),
                    getString(R.string.permission_denied),
                    getString(R.string.rational_storage_permission_explanation),
                    "Accept",
                    "No",
                    { requestPermissionLaunch(permissionForApiVersion()) },{})
            }
            else -> {
               requestPermissionLaunch(permissionForApiVersion())
            }
        }
    }
    fun checkCameraPermission(onSuccess: ()->Unit){
        when{
            isCheckCameraAccessGranted() ->{
                onSuccess()
            }
            isCameraPermissionRational()->{
                doubleButtonDialog(requireActivity(),
                    getString(R.string.permission_denied),
                    getString(R.string.rational_camera_permission_explanation),
                    "Accept",
                    "No",
                    { requestPermissionLaunch(cameraPermission) },{})
            }else->{
                requestPermissionLaunch(cameraPermission)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions->
        permissions.forEach(){actionMap->
            when(actionMap.key){
                cameraPermission[0]->{
                    if (actionMap.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                         openCamera() //should open for camera
                    }else{
                        checkMediaAccessPermission(){}
                    }
                }
                //image
                permissionForApiVersion()[0]->{
                    if (actionMap.value){
                        if(permissionRequestedByCamera){
                            openCamera()
                        }else {
                            openResultFragment()
                        }
                    }else{
                        checkMediaAccessPermission(){}
                    }
                }
                //audio
                permissionForApiVersion()[1]->{
                    if (actionMap.value){
                        if(permissionRequestedByCamera){
                            openCamera()
                        }else {
                            openResultFragment()
                        } //should open for audio
                    }else{
                        checkMediaAccessPermission(){}
                    }
                }
            }
        }

    }

    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        return when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedPhotoUri)
            else -> {
                val source = ImageDecoder.createSource(requireContext().contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
    }

    private fun requestPermissionLaunch(requests:Array<String>) {
        //Toast.makeText(requireContext(), "i am being called", Toast.LENGTH_SHORT).show()
        requestPermissionLauncher.launch(requests)
            //requestPermissionLauncher(permissionForApiVersion(),Constant.REQUEST_WRITE_EXTERNAL_STORAGE)
    }

    private fun isCheckSelfPermissionGranted(context: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(context, permissionForApiVersion()[0]) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(context, permissionForApiVersion()[1]) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isPermissionRational(context: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionForApiVersion()[0]) ||
                ActivityCompat.shouldShowRequestPermissionRationale(context, permissionForApiVersion()[1])}

    fun isCheckCameraAccessGranted():Boolean{
        return (ContextCompat.checkSelfPermission(requireContext(),cameraPermission[0]) == PackageManager.PERMISSION_GRANTED)
    }

    fun isCameraPermissionRational():Boolean{
    return ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),cameraPermission[0])
    }
    //endregion
}