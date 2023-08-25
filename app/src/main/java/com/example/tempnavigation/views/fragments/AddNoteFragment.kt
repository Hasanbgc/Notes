package com.example.tempnavigation.views.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.tempnavigation.R
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel
import com.google.android.material.textfield.TextInputEditText

class AddNoteFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private  val addNoteFragmentViewModel: AddNoteFragmentViewModel by viewModels()

    private  var rootView:View? = null
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var menuItem: Menu
    private lateinit var inputMethodManager: InputMethodManager

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

}