package com.example.tempnavigation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class HomeFragmentViewModel(application: Application): AndroidViewModel(application) {
    private val TAG ="HomeFragmentViewModel"
    private val noteRepository: NoteRepository

    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
    }
    val allNote = noteRepository.getAllNotes()

    fun insert(
        title: String,
        description: String,
        priority: Int,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.insert(NoteEntity(0,title, description, priority), onSuccess = {
            onSuccess()
        }, onFailed = {
            onFailed(it)
        })
    }

    fun update(
        id: Int,
        title: String,
        description: String,
        priority: Int,
        onSuccessUpdate: () -> Unit,
        onFailedUpdate: (message: String) -> Unit
    ) {
        noteRepository.getNoteById(id,
            onSuccess = { noteEntity ->
                noteEntity.title = title
                noteEntity.priority = priority
                noteEntity.description = description
                noteRepository.update(noteEntity,
                    onSuccess = {
                        onSuccessUpdate()
                    }, onFailed = {
                        onFailedUpdate(it)
                    })
            }, onFailed = {
                onFailedUpdate(it)
            })

    }

    fun delete(
        id: Int,
        title: String,
        description: String,
        priority: Int,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.delete(
            NoteEntity(id, title, description, priority),
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
        id: Int,
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
                NoteModel(noteEntity.id,noteEntity.title,noteEntity.description,noteEntity.priority)
            }!!
        }
        return modelList.toLiveData()
    }
}