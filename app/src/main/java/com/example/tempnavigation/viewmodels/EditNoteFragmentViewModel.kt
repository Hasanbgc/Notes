package com.example.tempnavigation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class EditNoteFragmentViewModel(application: Application):AndroidViewModel(application) {
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

    suspend fun getAllNotesAsync(
        onSuccess: (noteList:List<NoteEntity>, successMsg:String) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
//         noteRepository.getAllNotes(onSuccess = {noteList,msg->
//            //onSuccess(noteList,msg)
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
}