package com.example.tempnavigation.models

import com.example.tempnavigation.repositories.room.entity.NoteEntity

data class NoteModel(
    val id:Int,
    var title:String,
    var description:String,
    var priority:Int,
    var imageUri:String
){
    fun toNoteEntity() = NoteEntity(id,title,description,priority,imageUri)
}