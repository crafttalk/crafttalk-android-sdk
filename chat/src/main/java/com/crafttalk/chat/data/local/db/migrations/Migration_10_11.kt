package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration_10_11: Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Добавляем новое поле для meta.skipScore
        database.execSQL(
            "ALTER TABLE messages ADD COLUMN meta_skip_score TEXT"
        )
    }
}
