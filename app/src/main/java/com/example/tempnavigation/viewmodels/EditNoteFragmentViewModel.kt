package com.example.tempnavigation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tempnavigation.models.NoteModel
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
        noteModel: NoteModel,
        onSuccess: (id:String) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.insert(noteModel.toNoteEntity(), onSuccess = {id->
            onSuccess(id)
        }, onFailed = {
            onFailed(it)
        })
    }

    fun update(
        noteModel: NoteModel,
        onSuccessUpdate: () -> Unit,
        onFailedUpdate: (message: String) -> Unit
    ) {
        noteRepository.getNoteById(noteModel.id,
            onSuccess = { noteEntity ->
                noteEntity.title = noteModel.title
                noteEntity.description = noteModel.description
                noteEntity.imgUri = noteModel.imageUri
                noteEntity.locationLat = noteModel.locationLat
                noteEntity.locationLong = noteModel.locationLong
                noteEntity.alarmTime = noteModel.alarmTime
                noteEntity.savedTime = noteModel.savedTime
                noteEntity.archive = noteModel.archive
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
        noteModel: NoteModel,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.delete(noteModel.toNoteEntity(),
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


    /*fun getNote(
        id: Long,
        onSuccess: (note: NoteEntity) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.getNoteById(id,
            onSuccess = { note ->
                onSuccess(note)
            },
            onFailed = { onFailed(it) })
    }*/
}