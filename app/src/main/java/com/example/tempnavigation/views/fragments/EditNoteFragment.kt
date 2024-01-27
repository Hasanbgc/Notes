package com.example.tempnavigation.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.DateUtil
import com.example.tempnavigation.utilities.FileUtil
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.EditNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

class EditNoteFragment : Fragment(),View.OnClickListener {
    private val TAG = "EditNoteFragment"
    //region veriable
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: EditNoteFragmentViewModel by viewModels()
    private lateinit var rootView: View
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var discardButton: ImageView
    private lateinit var imageView: ImageView
    private lateinit var imageHolder: CardView
    private lateinit var imageFab: FloatingActionButton
    private lateinit var cameraFab: FloatingActionButton
    private lateinit var menuItem: Menu
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var bitmap: Bitmap
    private lateinit var imageUri:String
    private var imageTitle = ""
    private var id: Int = 0
    //endregion
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_note, container, false)
        inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setHasOptionsMenu(true)
        initView()
        setInitValue()
        observeLiveData()
        //setEditMenuVisibility()

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //menuItem = menu
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_note_menu, menu)
    }

    private fun setEditMenuVisibility() {

        editTextTitle.doAfterTextChanged {
            menuItem.findItem(R.id.save).isEnabled = editTextTitle.text.isNotEmpty()
            menuItem.findItem(R.id.delete).isEnabled = true
        }
    }

    private fun setInitValue() {
        mainViewModel.showBottomNav.value = false
        mainViewModel.title.value = "Edit Note"
    }

    private fun initView() {
        editTextTitle = rootView.findViewById(R.id.editText_title_edit)
        editTextDescription = rootView.findViewById(R.id.editText_description_edit)
        discardButton = rootView.findViewById(R.id.button_discard_edit)
        imageView = rootView.findViewById(R.id.image_view_edit)
        imageHolder = rootView.findViewById(R.id.image_holder_edit)
        imageFab = rootView.findViewById(R.id.fab_image_edit)
        cameraFab = rootView.findViewById(R.id.fab_camera_edit)
        discardButton = rootView.findViewById(R.id.button_discard_edit)
        imageFab.setOnClickListener(this)
        cameraFab.setOnClickListener(this)
        discardButton.setOnClickListener(this)
    }

    private fun observeLiveData() {
        mainViewModel.selectedNote.observe(viewLifecycleOwner, Observer { noteModel ->
            id = noteModel.id
            title = noteModel.title
            description = noteModel.description
            imageUri = noteModel.imageUri
            editTextTitle.setText(title)
            editTextDescription.setText(description)
            if(imageUri!="") {
                imageHolder.visibility = View.VISIBLE
                imageView.setImageURI(imageUri.toUri())
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_update -> updateNote()
            R.id.edit_delete -> {
                delete()
                mainViewModel.navigationPage.value = NavigationPage.HOME
            }
            android.R.id.home -> {
                hideSoftKeyboard()
                updateNote()
                mainViewModel.navigationPage.value = NavigationPage.HOME
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun delete() {
        hideSoftKeyboard()
        viewModel.delete(NoteModel(id,title,description,1,imageUri), {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "note deleted", Toast.LENGTH_SHORT).show()
                clear()
            }
        }, {})
    }

    private fun clear() {
        editTextTitle.text.clear()
        editTextDescription.text.clear()
    }

    private fun updateNote() {
        title = editTextTitle.text.trim().toString()
        description = editTextDescription.text.trim().toString()

        viewModel.update(NoteModel(id,title,description,1,imageUri), onSuccessUpdate = {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "note Updated", Toast.LENGTH_SHORT).show()
            }
        }, onFailedUpdate = {
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
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            imageView.setImageURI(uri)
            imageUri = uri.toString()
            imageHolder.visibility = View.VISIBLE
        }
    }
    private fun openResultFragment() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                bitmap = (result.data?.extras?.get("data") as? Bitmap)!!
                imageHolder.visibility = View.VISIBLE
                imageView.setImageBitmap(bitmap)
                imageTitle = DateUtil.getCurrentTime().toString()
                imageUri = FileUtil.saveBitmapToAppFolderAndGetPath(requireContext(),bitmap,imageTitle).toString()
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
//    private fun getCapturedImage(capturedImageUri: Uri): Bitmap {
//        return when {
//            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(requireContext().contentResolver, capturedImageUri)
//            else -> {
//                val source = ImageDecoder.createSource(requireContext().contentResolver, capturedImageUri)
//                ImageDecoder.decodeBitmap(source)
//            }
//        }
//    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fab_image_edit -> openResultFragment()
            R.id.fab_camera_edit -> openCamera()
            R.id.button_discard_edit->{
                imageView.setImageResource(0)
                imageUri = ""
                imageHolder.visibility = View.GONE
            }
        }
    }

}