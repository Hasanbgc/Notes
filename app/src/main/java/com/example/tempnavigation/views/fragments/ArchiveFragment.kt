package com.example.tempnavigation.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.tempnavigation.R
import com.example.tempnavigation.adapters.NoteViewAdapter
import com.example.tempnavigation.databinding.FragmentArchiveBinding
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel

class ArchiveFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val addNoteFragmentViewModel: AddNoteFragmentViewModel by viewModels()
    private lateinit var bindingArchiveFragment: FragmentArchiveBinding
    private lateinit var recyclerView:RecyclerView
    private lateinit var adapter:NoteViewAdapter

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingArchiveFragment =  FragmentArchiveBinding.inflate(inflater, container, false)
        rootView = bindingArchiveFragment.root
        initView()
        setInitValue()
        return rootView
    }
    private fun initView() {

    }
    private fun setInitValue() {
        mainViewModel.showBottomNav.value = true
        mainViewModel.title.value = "Archive"
        adapter  = NoteViewAdapter(requireContext(),addNoteFragmentViewModel){note->
            mainViewModel.selectedNote.value = note
            addNoteFragmentViewModel.setCurrentNote(note)
            mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE
        }
        bindingArchiveFragment.favRecyclerView.adapter = adapter
    }




}