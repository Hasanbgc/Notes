package com.example.tempnavigation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class AddNoteFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository

    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
    }
    val allNote = noteRepository.getAllNotes()

    fun insert(
        noteModel:NoteModel,
        onSuccess: () -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        val noteEntity =noteModel.toNoteEntity()//NoteEntity(note.id,note.title,note.description,note.priority,note.imageUri)
        noteRepository.insert(noteEntity, onSuccess = {
            onSuccess()
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
                noteEntity.priority = noteModel.priority
                noteEntity.description = noteModel.description
                noteEntity.imgUri = noteModel.imageUri
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
        val noteEntity = noteModel.toNoteEntity()
        noteRepository.delete(noteEntity,
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
        onSuccess: (noteList:List<NoteEntity>,successMsg:String) -> Unit,
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
    fun getNoteByTitle(
        title:String,
        onSuccess: (note: NoteModel) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        noteRepository.getNoteByTitle(title,
            onSuccess = { note ->
                onSuccess(note.toNoteModel())
            },
            onFailed = { onFailed(it) })
    }
}