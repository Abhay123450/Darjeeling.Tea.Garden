//package com.darjeelingteagarden.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
import androidx.room.RoomDatabase
//
//@Database(entities = [CartEntity::class], version = 1)
//abstract class CartDatabase: RoomDatabase() {
//
//    abstract fun cartDao(): CartDao
//
//    companion object{
//
//        @Volatile
//        private var INSTANCE: CartDatabase? = null
//
//        fun getCartDatabase(context: Context): CartDatabase{
//
//            val tempInstance = INSTANCE
//            if (tempInstance != null){
//                return tempInstance
//            }
//
//            synchronized(this){
//
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    CartDatabase::class.java,
//                    "cart"
//                ).build()
//                INSTANCE = instance
//                return instance
//
//            }
//        }
//    }
//
//}