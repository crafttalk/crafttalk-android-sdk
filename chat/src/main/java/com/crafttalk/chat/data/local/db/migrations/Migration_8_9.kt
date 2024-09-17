package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity


object Migration_8_9:Migration(8,9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS ${MessageEntity.TABLE_NAME}")
        database.execSQL("DROP TABLE IF EXISTS ${MessageEntity.TABLE_NAME_BACKUP}")
    }
}