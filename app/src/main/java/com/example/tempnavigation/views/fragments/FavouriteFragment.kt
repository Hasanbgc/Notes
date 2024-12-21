package com.example.tempnavigation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tempnavigation.adapters.NoteViewAdapter
import com.example.tempnavigation.databinding.FragmentFavouriteBinding
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.enums.NavigationPage
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel
import com.example.tempnavigation.viewmodels.MainViewModel

class FavouriteFragment : Fragment() {
    private val TAG = "FavouriteFragment"
    private val mainViewModel: MainViewModel by activityViewModels()
    private val addNoteFragmentViewModel:AddNoteFragmentViewModel by viewModels()
    private lateinit var bindingFavouriteFragment: FragmentFavouriteBinding
    private lateinit var adapter: NoteViewAdapter

    private lateinit var rootView: View
    private var list = mutableListOf<NoteModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bindingFavouriteFragment = FragmentFavouriteBinding.inflate(inflater,container,false)
        rootView = bindingFavouriteFragment.root
        initView()
        setInitValue()
        observeLiveData()
        return rootView
    }
    private fun initView(){

    }
    private fun setInitValue(){
        mainViewModel.showBottomNav.value = true
        mainViewModel.title.value = "Favourite"
        adapter  = NoteViewAdapter(requireContext(),addNoteFragmentViewModel)

        adapter.onItemClick = {note->
            mainViewModel.selectedNote.value = note
            addNoteFragmentViewModel.setCurrentNote(note)
            mainViewModel.navigationPage.value = NavigationPage.ADD_NOTE
        }

        bindingFavouriteFragment.favRecyclerView.adapter = adapter
    }
    fun observeLiveData(){
        mainViewModel.allNotes.observe(viewLifecycleOwner, Observer { data->
            val favoriteList = data.filter { it.favourite }.map { it.toNoteModel() }
            adapter.setNote(favoriteList)
        })
    }

}