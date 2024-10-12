package com.example.tempnavigation.models

import com.example.tempnavigation.repositories.room.entity.NoteEntity

data class NoteModel(
    val id:Long,
    var title:String,
    var description:String,
    var locationLat:Double,
    var locationLong:Double,
    var imageUri:String,
    var alarmTime:String,
    var savedTime:Long,
    var favourite:Boolean,
    var archive:Boolean
){
    fun toNoteEntity() = NoteEntity(
        id = id,
        title = title,
        description = description,
        locationLat = locationLat,
        locationLong = locationLong,
        imgUri = imageUri,
        alarmTime = alarmTime,
        savedTime = savedTime,
        favourite = favourite,
        archive = archive
        )
    fun isEmpty() = (id == 0L &&
            title == "" &&
            description == "" &&
            locationLat == 0.0 &&
            locationLong == 0.0 &&
            imageUri == "" &&
            alarmTime == "" &&
            savedTime == 0L && !favourite && !archive
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteModel

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (locationLat != other.locationLat) return false
        if (locationLong != other.locationLong) return false
        if (imageUri != other.imageUri) return false
        if (alarmTime != other.alarmTime) return false
        if (savedTime != other.savedTime) return false
        if (favourite != other.favourite) return false
        if (archive != other.archive) return false

        return true
    }

    companion object{
    fun emptyNote() = NoteModel(0L,"","",0.0,0.0,"","",0L,false,false)}
}