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
        SearchAlertEntity::class,
        com.example.data.local.entity.NotificationEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class QuickNestDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun notificationDao(): NotificationDao

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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
