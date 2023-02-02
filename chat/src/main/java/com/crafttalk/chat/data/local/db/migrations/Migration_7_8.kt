package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_7_8: Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // удаляю старую историю по следующим причинам:
        // * не можем корректно использовать эту историю с незаполненным полем namespace в нескольких чатах
        // * со включенной обфускацией до версии 1.2.24 в БД могут попадать обфусцированные теги (см. https://github.com/crafttalk/crafttalk-android-sdk/issues/10)
        database.execSQL("DELETE FROM ${MessageEntity.TABLE_NAME}")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN namespace TEXT NOT NULL DEFAULT ''")
    }
}
