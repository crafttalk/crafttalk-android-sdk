package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_5_6: Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN widget TEXT DEFAULT NULL")

    }
}