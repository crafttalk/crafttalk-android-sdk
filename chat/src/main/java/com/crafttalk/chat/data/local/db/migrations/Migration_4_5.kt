package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_4_5: Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN keyboard TEXT DEFAULT NULL")

    }
}