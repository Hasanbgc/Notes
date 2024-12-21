package com.example.tempnavigation.repositories.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import com.example.tempnavigation.repositories.room.entity.HomeViewEntity
import com.example.tempnavigation.utilities.Constant

@Dao
interface HomeViewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homeViewEntity: HomeViewEntity)
    @Update
    suspend fun update(homeViewEntity: HomeViewEntity)
    @Delete
    suspend fun delete(homeViewEntity: HomeViewEntity)

    @Query("SELECT * FROM ${Constant.TABLE_HOME_VIEW} WHERE id=1")
    fun getIsStaggered():HomeViewEntity?
}
