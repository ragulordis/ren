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
    version = 3,
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.propertyDao()?.insertProperties(
                                    DefaultProperties.sampleList.map { PropertyEntity.fromDomain(it) }
                                )
                                // Initial sample booked visits
                                INSTANCE?.propertyDao()?.insertVisit(
                                    VisitEntity(
                                        id = "visit-1",
                                        propertyId = "prop-1",
                                        propertyTitle = "2BHK Independent House with Covered Parking",
                                        location = "Kottakuppam",
                                        buyerName = "Ragul",
                                        date = "Tomorrow",
                                        timeSlot = "10:00 AM",
                                        status = "Confirmed"
                                    )
                                )
                                INSTANCE?.propertyDao()?.insertVisit(
                                    VisitEntity(
                                        id = "visit-2",
                                        propertyId = "prop-2",
                                        propertyTitle = "Beachfront 3BHK Villa with Plunge Pool",
                                        location = "Serenity Beach",
                                        buyerName = "Ragul",
                                        date = "This Saturday",
                                        timeSlot = "4:30 PM",
                                        status = "Requested"
                                    )
                                )
                                // Initial sample conversation message
                                INSTANCE?.propertyDao()?.insertMessage(
                                    ChatMessageEntity(
                                        id = "msg-sample-1",
                                        propertyId = "prop-1",
                                        senderName = "K. Balakrishnan",
                                        message = "Vanakkam Ragul! Thank you for your interest in the Kottakuppam house. The Patta and EC documents are 100% clear. Let me know when you'd like to inspect the site!",
                                        time = "10:30 AM",
                                        isFromMe = false,
                                        timestamp = System.currentTimeMillis() - 3600000
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
