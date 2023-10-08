package com.example.tempnavigation.repositories.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.repositories.room.entity.NoteEntity

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(noteEntity: NoteEntity)

    @Delete
    suspend fun delete(noteEntity: NoteEntity)

    @Update
    suspend fun update(noteEntity: NoteEntity)

    @Query("DELETE FROM ${Constant.TABLE_NOTE}")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM ${Constant.TABLE_NOTE} WHERE id=:id LIMIT  1")
    fun getNote(id:Int):NoteEntity
    @Query("SELECT * FROM ${Constant.TABLE_NOTE} WHERE id=:title LIMIT  1")
    fun getNoteByTitle(title:String):NoteEntity

    @Query("SELECT * FROM ${Constant.TABLE_NOTE} ORDER BY priority DESC")
    fun getAllNotes(): LiveData<List<NoteEntity>>
}