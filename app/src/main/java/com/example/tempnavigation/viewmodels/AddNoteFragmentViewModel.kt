package com.example.tempnavigation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class AddNoteFragmentViewModel(application: Application) : AndroidViewModel(application) {
    val TAG= "AddNoteFragmentViewModel"
    private val noteRepository: NoteRepository
    private var imageUri:String
    private lateinit var _currentNote: NoteModel

    var currentNote: NoteModel
        get() = _currentNote
        set(value) {
            _currentNote = value
            setImageUri(value.imageUri)
        }
    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
        imageUri = ""
       // currentNote = NoteModel.emptyNote()
    }
    val allNote = noteRepository.getAllNotes()

    fun setImageUri(uri:String){
        imageUri = uri
    }
   /* fun setCurrentNote(note: NoteModel) {
        Log.d(TAG, "setCurrentNote: $note")
        currentNote = note
        setImageUri(currentNote.imageUri)
    }

    fun getCurrentNote(): NoteModel {
        return currentNote
    }*/
    fun getImageUri() = imageUri
    fun insert(
        note:NoteModel,
        onSuccess: (id: String) -> Unit,
        onFailed: (message: String) -> Unit
    ) {
        val noteEntity =NoteEntity(title = note.title, description = note.description, locationLat = note.locationLat, locationLong = note.locationLong, imgUri = note.imageUri, alarmTime = note.alarmTime, savedTime = note.savedTime, favourite = note.favourite, archive = note.archive)
        noteRepository.insert(noteEntity, onSuccess = {id->
            onSuccess(id)
        }, onFailed = {
            onFailed(it)
        })
    }

    fun update(
        noteModel: NoteModel,
        onSuccessUpdate: (updateStatus:Boolean) -> Unit,
        onFailedUpdate: (message: String) -> Unit
    ) {
        noteRepository.update(noteModel.toNoteEntity(), onSuccess = {status->
            onSuccessUpdate(status)
        }, onFailed = {
            onFailedUpdate(it)
        })

        /*noteRepository.getNoteById(noteModel.id,
            onSuccess = { noteEntity ->
                noteEntity.title = noteModel.title
                noteEntity.description = noteModel.description
                noteEntity.imgUri = noteModel.imageUri
                noteEntity.locationLat = noteModel.locationLat
                noteEntity.locationLong = noteModel.locationLong
                noteEntity.alarmTime = noteModel.alarmTime
                noteEntity.savedTime = noteModel.savedTime
                noteEntity.favourite = noteModel.favourite
                noteEntity.archive = noteModel.archive
                noteRepository.update(noteEntity,
                    onSuccess = {status->
                        onSuccessUpdate(status)
                    }, onFailed = {
                        onFailedUpdate(it)
                    })
            }, onFailed = {
                onFailedUpdate(it)
            })*/

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
        id: String,
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

    fun updateCurrentNoteUsingNewID(id: String){
        getNote(id,{ noteEntity ->
            Log.d(TAG, "updateCurrentNoteUsingNewID: noteEntity = $noteEntity ")
                currentNote = noteEntity.toNoteModel()
        },{})
    }
}