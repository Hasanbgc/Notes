package com.example.tempnavigation.repositories.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tempnavigation.utilities.Constant

@Entity(tableName = Constant.TABLE_HOME_VIEW)
class HomeViewEntity (
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val isStaggered: Boolean
    )