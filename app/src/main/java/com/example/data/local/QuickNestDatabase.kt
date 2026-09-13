package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PropertyEntity::class,
        VisitEntity::class,
        ChatMessageEntity::class,
        PropertyReportEntity::class,
        SearchAlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuickNestDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao

    companion object {
        @Volatile
        private var INSTANCE: QuickNestDatabase? = null

        fun getInstance(context: Context): QuickNestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuickNestDatabase::class.java,
                    "quicknest_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
