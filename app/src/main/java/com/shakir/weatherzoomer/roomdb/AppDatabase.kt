package com.shakir.weatherzoomer.roomdb

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