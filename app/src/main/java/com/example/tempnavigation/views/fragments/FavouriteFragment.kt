package com.example.tempnavigation.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tempnavigation.R
import com.example.tempnavigation.viewmodels.MainViewModel

class FavouriteFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_favourite, container, false)
        initView()
        setInitValue()
        return rootView
    }
    private fun initView(){

    }
    private fun setInitValue(){
        mainViewModel.showBottomNav.value = true
        mainViewModel.title.value = "Favourite"
    }
}