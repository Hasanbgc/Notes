package com.example.tempnavigation.repositories.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tempnavigation.repositories.room.dao.HomeViewDao
import com.example.tempnavigation.utilities.Constant
import com.example.tempnavigation.repositories.room.dao.NoteDao
import com.example.tempnavigation.repositories.room.entity.HomeViewEntity
import com.example.tempnavigation.repositories.room.entity.NoteEntity

@Database(entities = [NoteEntity::class,HomeViewEntity::class], version = Constant.DB_Version, exportSchema = false)
abstract class NoteRoomDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun homeViewDao():HomeViewDao

    companion object {
        @Volatile
        private var INSTANCE: NoteRoomDatabase? = null
        fun getDatabase(context: Context): NoteRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        NoteRoomDatabase::class.java,
                        Constant.DB_NAME
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
