package com.example.tempnavigation.repositories

import android.util.Log
import com.example.tempnavigation.repositories.room.dao.HomeViewDao
import com.example.tempnavigation.repositories.room.entity.HomeViewEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewRepository(private val homeViewDao: HomeViewDao) {
    private val TAG = "HomeViewRepository"
    fun insert(homeViewEntity: HomeViewEntity){
        insertHomeViewAsyncTask(homeViewEntity)
    }

    fun update(homeViewEntity:HomeViewEntity){
        updateHomeViewAsyncTask(homeViewEntity)
        Log.d(TAG, "$TAG update: ${homeViewEntity.isStaggered}")
    }
//    fun delete(homeViewEntity: HomeViewEntity){
//        homeViewDao.delete(homeViewEntity)
//    }
    fun getViewState():Boolean?{
        val homeViewEntity= homeViewDao.getIsStaggered()
        return homeViewEntity?.isStaggered
    }

    private fun insertHomeViewAsyncTask(homeView: HomeViewEntity){
        CoroutineScope(Dispatchers.IO).launch {
            homeViewDao.insert(homeView)
        }.invokeOnCompletion { throwable->
            if (throwable != null) {
                throw Exception(throwable.message)
            }
        }

    }
    private fun updateHomeViewAsyncTask(homeView: HomeViewEntity){
        CoroutineScope(Dispatchers.IO).launch {
            homeViewDao.update(homeView)
        }.invokeOnCompletion { throwable->
            if (throwable != null) {
                throw Exception(throwable.message)
            }
        }

    }

}