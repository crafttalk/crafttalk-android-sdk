package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_3_4: Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN dialog_id TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_id TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_text TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_span_structure_list TEXT NOT NULL DEFAULT '[]'")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_url TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_type TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_name TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_size INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_download_progress_type TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN repliedMessageAttachmentHeight INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE ${MessageEntity.TABLE_NAME} ADD COLUMN replied_message_attachment_width INTEGER DEFAULT NULL")

    }
}