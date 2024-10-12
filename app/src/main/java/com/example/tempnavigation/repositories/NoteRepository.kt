package com.example.tempnavigation.repositories


import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
import com.example.tempnavigation.repositories.room.dao.NoteDao
import com.example.tempnavigation.repositories.room.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlin.math.log

class NoteRepository(private var noteDao: NoteDao) {

    private val TAG = "NoteRepository"

    //private val allNotes:LiveData<List<NoteEntity>>
//    init {
//        val noteDb: NoteRoomDatabase = NoteRoomDatabase.getDatabase(application)
//        noteDao = noteDb.noteDao()
//        //allNotes =
//    }
    fun insert(note: NoteEntity, onSuccess:(id:Long)->Unit, onFailed: (message: String) -> Unit){
        insertNoteAsyncTask(note, onSuccess = {id ->
            onSuccess(id)
        }, onFailed = { it->
            onFailed(it)
        })
    }
    fun update(note: NoteEntity,onSuccess: (updateStatus:Boolean) -> Unit,onFailed: (message: String) -> Unit) {
        updateNoteAsyncTask(note,onSuccess= {status->
           onSuccess(status)
        },onFailed={
            onFailed(it)
        })
    }

    fun delete(note: NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit) {
        deleteNoteAsyncTask(note, onSuccess = {
            Handler(Looper.getMainLooper()).post {
                onSuccess()
            }
        }, onFailed = {
            Handler(Looper.getMainLooper()).post {
                onFailed(it)
            }
        })
    }
    fun getNoteById(id:Long,onSuccess: (noteEntity:NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
        getNote(id,onSuccess={noteEntity->
            onSuccess(noteEntity)
        }, onFailed = {

        })
    }
    fun getNoteByTitle(title: String,onSuccess: (noteEntity:NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
        getNotebyTitle(title,onSuccess={noteEntity->
            onSuccess(noteEntity)
        }, onFailed = {

        })
    }
    fun deleteAllNotes(onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        deleteAllNoteAsyncTask(onSuccess = {
            onSuccess()
        },onFailed={
            onFailed(it)
        })
    }
    suspend fun getAllNotesAsync(onSuccess: (LiveData<List<NoteEntity>>,message:String) -> Unit, onFailed: (message: String) -> Unit){

        getAllNoteAsyncTask(onSuccess = {noteList,msg->
          onSuccess(noteList,msg)
        }, onFailed = {
           onFailed(it)
       })
    }
    fun getAllNotes() = noteDao.getAllNotes()


    private fun insertNoteAsyncTask(note: NoteEntity,onSuccess: (id:Long) ->Unit,onFailed:(message:String)->Unit){
        var id = 0L
        CoroutineScope(IO).launch {
            id = noteDao.insert(note)
        }.invokeOnCompletion { throwable->
        if (throwable == null) {
            onSuccess(id)
        }else{
            onFailed(throwable.message.toString())
            throw Exception(throwable.message)
        }
        }

    }
    private fun updateNoteAsyncTask(note:NoteEntity,onSuccess: (updateStatus:Boolean) -> Unit,onFailed: (message: String) -> Unit){
        CoroutineScope(IO).launch {
            noteDao.update(note)
        }.invokeOnCompletion { throwable->
            if(throwable == null) {
            onSuccess(true)
            }
            else{
                onFailed(throwable.message.toString())
                throw Exception(throwable.message.toString())
            }
        }
    }
    private fun deleteNoteAsyncTask(note:NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        CoroutineScope(IO).launch {
            Log.d("NoteRepo","$note")
            noteDao.deleteByID(note.id)
        }.invokeOnCompletion { throwable->
            if(throwable == null) {
                Handler(Looper.getMainLooper()).post {
                    Log.d(TAG,"throwable = $throwable")
                    onSuccess()
                }
            }
            else{
                Handler(Looper.getMainLooper()).post {
                    onFailed(throwable.message.toString())
                    throw Exception(throwable.message.toString())
                }
            }
        }
    }
    private fun deleteAllNoteAsyncTask(onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        CoroutineScope(IO).launch {
            noteDao.deleteAllNotes()
        }.invokeOnCompletion { throwable->
            if(throwable == null) {
                onSuccess()
            }
            else{
                onFailed(throwable.message.toString())
                throw Exception(throwable.message.toString())
            }
        }
    }
    private suspend fun getAllNoteAsyncTask(onSuccess: (LiveData<List<NoteEntity>>,msg:String) -> Unit, onFailed: (message: String) -> Unit){
        withContext(Dispatchers.IO) {
           try{
               val noteList = noteDao.getAllNotes()
               withContext(Dispatchers.Main){
                   onSuccess(noteList,"Successfully got the list")
               }
           }catch (e:Exception){
               withContext(Dispatchers.Main){
                   e?.message?.let { onFailed(it) }
               }
           }
        }
    }
    private fun getNote(id:Long,onSuccess: (note: NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
        var noteEntity  =NoteEntity(0,"","",0.0,0.0,"","",0,false,false)
        CoroutineScope(IO).launch {
           noteEntity = noteDao.getNote(id)
        }.invokeOnCompletion { throwable->
            if(throwable==null){
                onSuccess(noteEntity)
            }else{
                onFailed(throwable.message.toString())
            }
        }
    }
    private fun getNotebyTitle(title:String,onSuccess: (note: NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
        var noteEntity  =NoteEntity(0,"","",0.0,0.0,"","",0,false,false)
        CoroutineScope(IO).launch {
            Log.d(TAG, "getNotebyTitle: $title")
            noteEntity = noteDao.getNoteByTitle(title)

        }.invokeOnCompletion { throwable->
            if(throwable==null){
                Log.d(TAG, "getNotebyTitle: $noteEntity")
                onSuccess(noteEntity)

            }else{
                onFailed(throwable.message.toString())
            }
        }
    }


}