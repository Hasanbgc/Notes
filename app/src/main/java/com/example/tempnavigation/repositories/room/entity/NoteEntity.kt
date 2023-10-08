package com.example.tempnavigation.repositories.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.Constant

@Entity(tableName = Constant.TABLE_NOTE)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    var title:String,
    var description:String,
    var priority:Int,
    var imgUri:String
){
    fun toNoteModel() = NoteModel(id,title,description,priority,imgUri)
}
