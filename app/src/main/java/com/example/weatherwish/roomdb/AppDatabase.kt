package com.example.weatherwish.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//@Database(entities = [MyResult::class], version = 1)
//abstract class AppDatabase : RoomDatabase() {
//
//    abstract fun roomDao(): RoomDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            if (INSTANCE == null) {
//                synchronized(this) {
//                    INSTANCE = Room.databaseBuilder(context,
//                        AppDatabase::class.java,
//                        "quoteDB")
//                        .build()
//                }
//            }
//            return INSTANCE!!
//        }
//
//    }
//
//}