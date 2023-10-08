package com.example.tempnavigation.repositories


import androidx.lifecycle.LiveData
import com.example.tempnavigation.repositories.room.dao.NoteDao
import com.example.tempnavigation.repositories.room.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {

    private val TAG = "NoteRepository"
    //private val allNotes:LiveData<List<NoteEntity>>
//    init {
//        val noteDb:NoteRoomDatabase = NoteRoomDatabase.getDatabase(application)
//        noteDao = noteDb.noteDao()
//        //allNotes =
//    }
    fun insert(note: NoteEntity, onSuccess:()->Unit, onFailed: (message: String) -> Unit){
        insertNoteAsyncTask(note, onSuccess = {
            onSuccess()
        }, onFailed = { it->
            onFailed(it)
        })
    }
    fun update(note: NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit) {
        updateNoteAsyncTask(note,onSuccess= {
           onSuccess()
        },onFailed={
            onFailed(it)
        })
    }
    fun delete(note: NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit) {
        deleteNoteAsyncTask(note, onSuccess = {

        }, onFailed = {
            onFailed(it)
        })
    }
    fun getNoteById(id:Int,onSuccess: (noteEntity:NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
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


    private fun insertNoteAsyncTask(note: NoteEntity,onSuccess: () ->Unit,onFailed:(message:String)->Unit){

        CoroutineScope(IO).launch {
            noteDao.insert(note)
        }.invokeOnCompletion { throwable->
        if (throwable == null) {
            onSuccess()
        }else{
            onFailed(throwable.message.toString())
            throw Exception(throwable.message)
        }
        }

    }
    private fun updateNoteAsyncTask(note:NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        CoroutineScope(IO).launch {
            noteDao.update(note)
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
    private fun deleteNoteAsyncTask(note:NoteEntity,onSuccess: () -> Unit,onFailed: (message: String) -> Unit){
        CoroutineScope(IO).launch {
            noteDao.delete(note)
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
    private fun getNote(id:Int,onSuccess: (note: NoteEntity) -> Unit,onFailed: (message: String) -> Unit){
        var noteEntity  =NoteEntity(0,"","",0,"")
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
        var noteEntity  =NoteEntity(0,"","",0,"")
        CoroutineScope(IO).launch {
            noteEntity = noteDao.getNoteByTitle(title)
        }.invokeOnCompletion { throwable->
            if(throwable==null){
                onSuccess(noteEntity)
            }else{
                onFailed(throwable.message.toString())
            }
        }
    }


}