package com.example.tempnavigation.viewmodels

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.enums.NavigationPage

class MainViewModel(application: Application) : AndroidViewModel(application) {


    val title = MutableLiveData<String>().apply { value = "" }
    val navigationPage = MutableLiveData<NavigationPage>().apply { value = NavigationPage.NOTHING }
    val showBottomNav = MutableLiveData<Boolean>().apply { value = true }
    val selectedLocation = MutableLiveData<Pair<Double,Double>>().apply { value = Pair(0.0,0.0) }
    init {

    }
    val selectedNote= MutableLiveData<NoteModel>().apply { value = NoteModel(0,"","",0,"") }


}