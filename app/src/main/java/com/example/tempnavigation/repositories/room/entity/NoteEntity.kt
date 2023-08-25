package com.example.tempnavigation.repositories.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tempnavigation.utilities.Constant

@Entity(tableName = Constant.TABLE_NOTE)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    var title:String,
    var description:String,
    var priority:Int
)
