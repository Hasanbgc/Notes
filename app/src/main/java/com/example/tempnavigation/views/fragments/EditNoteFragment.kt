package com.example.tempnavigation.views.fragments

import android.content.Context
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
import androidx.lifecycle.Observer
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.EditNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel

class EditNoteFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: EditNoteFragmentViewModel by viewModels()
    private lateinit var rootView: View
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var menuItem: Menu
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var note: NoteModel
    private lateinit var title: String
    private lateinit var description: String
    private var id: Int = 0
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
        editTextTitle = rootView.findViewById(R.id.editText_title)
        editTextDescription = rootView.findViewById(R.id.editText_description)
    }

    private fun observeLiveData() {
        mainViewModel.selectedNote.observe(viewLifecycleOwner, Observer { noteModel ->
            id = noteModel.id
            title = noteModel.title
            description = noteModel.description
            editTextTitle.setText(title)
            editTextDescription.setText(description)
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
        viewModel.delete(id, title, description, 1, {
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

        viewModel.update(id, title, description, 1, onSuccessUpdate = {
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
}