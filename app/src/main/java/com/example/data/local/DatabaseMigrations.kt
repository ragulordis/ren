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
