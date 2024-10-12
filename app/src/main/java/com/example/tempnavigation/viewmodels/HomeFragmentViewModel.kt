package com.example.tempnavigation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.HomeViewRepository
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.HomeViewEntity
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class HomeFragmentViewModel(application: Application,private val savedStateHandle: SavedStateHandle): AndroidViewModel(application) {
    private val TAG ="HomeFragmentViewModel"
    private val noteRepository: NoteRepository
    private val homeViewRepositroy:HomeViewRepository
    private var isStaggrated:Boolean = true
    private val KEY_BOOLEAN = "boolean_key"
    val longPressed = MutableLiveData<Boolean>().apply { false }
    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
        homeViewRepositroy = HomeViewRepository(noteDb.homeViewDao())
    }
    var allNote = noteRepository.getAllNotes()
    fun updateViewState(viewState:Boolean){
        val homeViewEntity = HomeViewEntity(1,viewState)
        homeViewRepositroy.update(homeViewEntity)
    }
    fun getHomeViewStyle() = homeViewRepositroy.getViewState()

    fun insert(
        noteModel: NoteModel,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.insert(noteModel.toNoteEntity(), onSuccess = {
            onSuccess()
        }, onFailed = {
            onFailed(it)
        })
    }

//    fun update(
//        id: Long,
//        title: String,
//        description: String,
//        location: Pair<Double,Double>,
//        onSuccessUpdate: () -> Unit,
//        onFailedUpdate: (message: String) -> Unit
//    ) {
//        noteRepository.getNoteById(id,
//            onSuccess = { noteEntity ->
//                noteEntity.title = title
//                noteEntity.locationLat = location.first
//                noteEntity.locationLong = location.second
//                noteEntity.description = description
//                noteRepository.update(noteEntity,
//                    onSuccess = {
//                        onSuccessUpdate()
//                    }, onFailed = {
//                        onFailedUpdate(it)
//                    })
//            }, onFailed = {
//                onFailedUpdate(it)
//            })
//
//    }
    fun update(note:NoteModel,onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        noteRepository.update(note.toNoteEntity(),{ onSuccess() },{ onFailed(it) })
    }

    fun delete(
       noteModel: NoteModel,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        val functionName = Throwable().stackTrace[1].methodName
        Log.d("HomeViewModel","called by $functionName")
        noteRepository.delete(
           noteModel.toNoteEntity(),
            onSuccess = {
                onSuccess()
            }, onFailed = {
                onFailed(it)
            })
    }

    fun deleteAll(
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit) {
        noteRepository.deleteAllNotes(onSuccess = {
            onSuccess()
        }, onFailed = {
            onFailed(it)
        })
    }

    suspend fun getAllNotes(
        onSuccess: (noteList:LiveData<List<NoteModel>>, successMsg:String) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
//        noteRepository.getAllNotes(onSuccess = {noteList,msg->
//            noteList.value?.let {noteEntityList->
//                val modelList = noteEntityList.map { noteEntity ->
//                    NoteModel(noteEntity.id,noteEntity.title,noteEntity.description,noteEntity.priority)
//                }
//                onSuccess(modelList.toLiveData(), msg)
//            }
//        }, onFailed = {errorMsg->
//            onFailed(errorMsg)
//        })

    }

    fun getNote(
        id: Long,
        onSuccess: (note: NoteEntity) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.getNoteById(id,
            onSuccess = { note ->
                onSuccess(note)
            },
            onFailed = { onFailed(it) })
    }
    fun <T> List<T>.toLiveData(): LiveData<List<T>> {
        val mutableLiveData = MutableLiveData<List<T>>()
        mutableLiveData.value = this
        return mutableLiveData
    }
    fun toModel(list:LiveData<List<NoteModel>>):LiveData<List<NoteModel>>{
        var modelList =  listOf<NoteModel>()
        list.value.let {noteEntityList->
            modelList = noteEntityList?.map { noteEntity->
                NoteModel(noteEntity.id,noteEntity.title,noteEntity.description,noteEntity.locationLat,noteEntity.locationLong,noteEntity.imageUri,noteEntity.alarmTime,noteEntity.savedTime,noteEntity.favourite,noteEntity.archive)
            }!!
        }
        return modelList.toLiveData()
    }
}