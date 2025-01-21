package com.example.tempnavigation.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tempnavigation.R
import com.example.tempnavigation.adapters.ToDoListAdapter
import com.example.tempnavigation.databinding.FragmentSettingBinding
import com.example.tempnavigation.models.ToDoItemModel
import com.example.tempnavigation.utilities.LocationManager
import com.example.tempnavigation.viewmodels.MainViewModel
import com.wada811.viewbindingktx.viewBinding


class SettingFragment : Fragment(R.layout.fragment_setting) {

    val TAG = "SettingFragment"
    private val mainViewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentSettingBinding::bind)
    private val adapter by lazy { ToDoListAdapter() }
    private val locationManager by lazy { LocationManager(requireContext()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initValue()
    }

    private fun initValue() {
        val data = listOf(ToDoItemModel(0,"",true))
        adapter.submitList(data)
        /*locationManager.startLocationUpdates()
        locationManager.setLocationUpdateListener(object :LocationManager.LocationUpdateListener{
            override fun onLocationUpdated(latitude: Double, longitude: Double) {
                Log.e(TAG, "onLocationUpdated: lat = $latitude lon = $longitude")
            }
        })*/
    }

    private fun initView() {
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
            recyclerView.addItemDecoration(dividerItemDecoration)
            recyclerView.adapter = adapter
        }
    }


}