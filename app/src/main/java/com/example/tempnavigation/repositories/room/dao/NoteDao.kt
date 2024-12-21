package com.example.tempnavigation.repositories.room.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.example.tempnavigation.repositories.room.NoteRoomDatabase
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

    /*@Query("DELETE FROM ${Constant.TABLE_NOTE}")
    suspend fun deleteAllNotes(): Boolean*/

    @Query("DELETE FROM ${Constant.TABLE_NOTE} WHERE id=:id")
    suspend fun deleteByID(id:String):Int

    @Query("SELECT * FROM ${Constant.TABLE_NOTE} WHERE id=:id LIMIT  1")
    fun getNote(id: String):NoteEntity

    @Query("SELECT * FROM ${Constant.TABLE_NOTE} WHERE id=:title LIMIT  1")
    fun getNoteByTitle(title:String):NoteEntity

    @Query("SELECT * FROM ${Constant.TABLE_NOTE} ORDER BY savedTime DESC")
    fun getAllNotes(): LiveData<List<NoteEntity>>
}