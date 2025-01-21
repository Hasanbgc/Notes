package com.example.tempnavigation.repositories.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.Constant
import java.util.UUID

@Entity(tableName = Constant.TABLE_NOTE)
data class NoteEntity(
    @PrimaryKey()
    var id: String = UUID.randomUUID().toString(),
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
) {
    fun toNoteModel() = NoteModel(
        id.toString(),
        title,
        description,
        locationLat,
        locationLong,
        imgUri,
        alarmTime,
        savedTime,
        favourite,
        archive
    )

    companion object {
        fun emptyNoteEntity() = NoteEntity(
            title = "",
            description = "",
            locationLat = 0.0,
            locationLong = 0.0,
            imgUri = "",
            alarmTime = "",
            savedTime = 0L,
            favourite = false,
            archive = false
        )
    }
}