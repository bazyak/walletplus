package com.bazyak.walletplus.wear.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WearPassEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WearPassDatabase : RoomDatabase() {
    abstract fun wearPassDao(): WearPassDao

    companion object {
        @Volatile
        private var INSTANCE: WearPassDatabase? = null

        fun getInstance(context: Context): WearPassDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                WearPassDatabase::class.java,
                "wear_pass_database",
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
