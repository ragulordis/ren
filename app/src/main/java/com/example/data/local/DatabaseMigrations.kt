package com.example.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE properties ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Adds buyerId and sellerId to the visits table.
 * Required for Firestore security rules that validate request.auth.uid == buyerId on write.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE visits ADD COLUMN buyerId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE visits ADD COLUMN sellerId TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Adds senderId to the chat_messages table to bind messages authoritatively to the sender's auth UID.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE chat_messages ADD COLUMN senderId TEXT NOT NULL DEFAULT ''")
    }
}

/**
 * Creates the notifications table for the in-app notification system.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `notifications` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `title` TEXT NOT NULL,
                `message` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `isRead` INTEGER NOT NULL DEFAULT 0,
                `propertyId` TEXT,
                `targetLocation` TEXT,
                `actionText` TEXT
            )
        """.trimIndent())
    }
}

