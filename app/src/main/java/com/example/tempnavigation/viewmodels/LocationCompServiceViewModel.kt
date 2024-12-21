package com.example.tempnavigation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tempnavigation.repositories.NoteRepository
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.entity.NoteEntity

class LocationCompServiceViewModel (application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository

    init {
        val noteDb = NoteRoomDatabase.getDatabase(application)
        noteRepository = NoteRepository(noteDb.noteDao())
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


}