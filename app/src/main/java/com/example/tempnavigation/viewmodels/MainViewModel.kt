package com.example.tempnavigation.viewmodels

import android.app.Application
import android.location.Location
import android.provider.ContactsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.HomeViewRepository
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.HomeViewEntity
import com.example.tempnavigation.utilities.enums.NavigationPage

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG ="HomeFragmentViewModel"
    private val noteRepository: NoteRepository
    private val homeViewRepository: HomeViewRepository

    val title = MutableLiveData<String>().apply { value = "" }
    val navigationPage = MutableLiveData<NavigationPage>().apply { value = NavigationPage.NOTHING }
    val showBottomNav = MutableLiveData<Boolean>().apply { value = true }
    val selectedLocation = MutableLiveData<Pair<Double,Double>>().apply { value = Pair(0.0,0.0) }
    val selectedNote= MutableLiveData<NoteModel>().apply { value = NoteModel.emptyNote() }



    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
        homeViewRepository = HomeViewRepository(noteDb.homeViewDao())
    }

    val allNotes = noteRepository.getAllNotes()

     fun insertInitialHomeViewStyle(){
        val entity = HomeViewEntity(1,false)
        homeViewRepository.insert(entity)
    }
}