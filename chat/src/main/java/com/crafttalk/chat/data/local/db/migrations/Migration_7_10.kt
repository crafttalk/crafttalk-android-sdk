package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_7_10: Migration(7, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("DELETE FROM ${MessageEntity.TABLE_NAME}")

    }
}