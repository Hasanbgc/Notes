package com.example.tempnavigation.repositories.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.Constant

@Entity(tableName = Constant.TABLE_NOTE)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    var id:Long,
    @ColumnInfo(name = "title")
    var title:String,
    @ColumnInfo(name = "description")
    var description:String,
    @ColumnInfo(name = "locationLat")
    var locationLat:Double,
    @ColumnInfo(name = "locationLong")
    var locationLong:Double,
    @ColumnInfo(name = "imageURL")
    var imgUri:String,
    @ColumnInfo(name = "alarmTime")
    var alarmTime:String,
    @ColumnInfo(name = "savedTime")
    var savedTime:Long,
    @ColumnInfo(name = "favourite")
    var favourite:Boolean,
    @ColumnInfo(name = "archive")
    var archive:Boolean,
){
    fun toNoteModel() = NoteModel(id,title,description,locationLat,locationLong,imgUri,alarmTime,savedTime,favourite,archive)
}