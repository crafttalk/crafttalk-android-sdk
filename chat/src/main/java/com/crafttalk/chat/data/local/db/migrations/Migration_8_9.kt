package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity


object Migration_8_9:Migration(8,9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM " +  MessageEntity.TABLE_NAME);
    }
}